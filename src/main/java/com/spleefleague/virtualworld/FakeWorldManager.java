/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld;

import com.spleefleague.virtualworld.api.implementation.BlockChange;
import com.spleefleague.virtualworld.api.implementation.BlockChange.ChangeType;
import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.api.FakeWorld;
import com.spleefleague.virtualworld.api.implementation.FakeBlockBase;
import com.spleefleague.virtualworld.api.implementation.FakeChunkBase;
import com.spleefleague.virtualworld.api.implementation.FakeWorldBase;
import com.spleefleague.virtualworld.protocol.MultiBlockChangeHandler;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldEvent;
import net.minecraft.server.v1_12_R1.SoundEffectType;
import net.minecraft.server.v1_12_R1.SoundCategory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author jonas
 */
public class FakeWorldManager implements Listener {
    
    private final Map<Player, Map<FakeWorld, Integer>> observedWorlds;
    private final MultiBlockChangeHandler mbchandler;
    
    private FakeWorldManager(MultiBlockChangeHandler mbchandler) {
        this.observedWorlds = new ConcurrentHashMap<>();
        this.mbchandler = mbchandler;
    }
    
    public Collection<FakeBlock> getBlocksInChunk(Player player, int x, int z) {
        return observedWorlds.get(player)
                .entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))//From high priority to high
                .map(e -> ((FakeWorldBase)e.getKey()).getChunkAtRaw(x, z))
                .filter(c -> c != null)
                .flatMap(c -> c.getUsedBlocks().stream())
                .distinct()
                .collect(Collectors.toSet());
    }
    
    public FakeWorld getWorldAt(Player player, Location l) {
        return observedWorlds.get(player)
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getHandle() == l.getWorld())
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .map(e -> e.getKey())
                .filter(fw -> fw.getArea() == null || fw.getArea().isInside(l.toVector()))
                .findFirst()
                .orElse(null);
                
    }
    
    public FakeBlock getBlockAt(Player player, Location l) {
        return getBlockAt(player, l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }
    
    public FakeBlock getBlockAt(Player player, World world, int x, int y, int z) {
        FakeBlock fb =  observedWorlds.get(player)
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getHandle() == world)
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .map(e -> e.getKey())
                .map(fw -> ((FakeWorldBase)fw).getBlockAtRaw(x, y, z))
                .filter(o -> o != null)
                .findFirst()
                .orElse(null);
        return fb;
    }
    
    public void addWorld(Player player, FakeWorld world, int priority) {
        Map<FakeWorld, Integer> worlds = observedWorlds.get(player);
        if(!worlds.containsKey(world)) {
            worlds.put(world, priority);
            Collection<FakeBlock> newChanges = ((FakeWorldBase)world).getUsedBlocks()
                    .stream()
                    .filter(fb -> getBlockAt(player, fb.getLocation()).getWorld() == world)
                    .collect(Collectors.toSet());
            sendDirect(player, newChanges);
        }
    }
    
    public void removeWorld(FakeWorld world) {
        observedWorlds.keySet().forEach(p -> removeWorld(p, world));
    }
    
    public void removeWorld(Player player, FakeWorld world) {
        Map<FakeWorld, Integer> worlds = observedWorlds.get(player);
        if(worlds.containsKey(world)) {
            worlds.remove(world);
            Collection<FakeBlock> newChanges =((FakeWorldBase)world).getUsedBlocks()
                    .stream()
                    .map(fb -> {
                        FakeBlock replacement = getBlockAt(player, fb.getLocation());
                        if(replacement == null) {
                            //Fake block only to be used to hold location + material.
                            replacement = new FakeBlockBase((FakeChunkBase)world.getChunkAt(fb.getX() / 16, fb.getZ() / 16), fb.getX(), fb.getY(), fb.getZ());
                            ((FakeBlockBase)replacement)._setType(replacement.getHandle().getType());
                            ((FakeBlockBase)replacement)._setData(replacement.getHandle().getData());
                        }
                        return replacement;
                    })
                    .collect(Collectors.toSet());
            sendDirect(player, newChanges);
        }
    }
    
    public FakeWorld createWorld(World world) {
        return createWorld(world, null);
    }
    
    public FakeWorld createWorld(World world, Area area) {
        return new FakeWorldBase(world, area);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        observedWorlds.remove(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        observedWorlds.put(event.getPlayer(), new ConcurrentHashMap<>());
    }
    
    private void startBlockCheckLoop() {
        Bukkit.getScheduler().runTaskTimer(VirtualWorld.getInstance(), () -> {
            for(Player player : observedWorlds.keySet()) {
                Map<ChangeType, Set<FakeBlock>> changes = getChangesPerPlayer(player);
                sendDirect(player, changes.get(ChangeType.PLUGIN));
                sendBreak(player, changes.get(ChangeType.BREAK));
                sendPlace(player, changes.get(ChangeType.PLACE));
            }
            observedWorlds.values()
                    .stream()
                    .flatMap(m -> m.keySet().stream())
                    .forEach(fw -> ((FakeWorldBase)fw).getChanges().clear());
        }, 0, 1);
    }
    
    private void sendDirect(Player player, Collection<? extends FakeBlock> blocks) {
        if(blocks == null || blocks.isEmpty()) return;
        mbchandler.changeBlocks(blocks, player);
    }
    
    private void sendBreak(Player player, Collection<? extends FakeBlock> blocks) {
        if(blocks == null || blocks.isEmpty()) return;
        mbchandler.changeBlocks(blocks, player);
        EntityPlayer entity = ((CraftPlayer) player).getHandle();
        for(FakeBlock block : blocks) {
            SoundEffectType effectType = breakSounds.get(block.getType());
            entity.a(effectType.d(), effectType.a() * 0.15f, effectType.b());
            PacketPlayOutWorldEvent particles = new PacketPlayOutWorldEvent(2001, new net.minecraft.server.v1_12_R1.BlockPosition(block.getX(), block.getY(), block.getZ()), block.getType().getId(), false);
            entity.playerConnection.sendPacket((Packet) particles);
        }
    }
    
    private void sendPlace(Player player, Collection<? extends FakeBlock> blocks) {
        if(blocks == null || blocks.isEmpty()) return;
        mbchandler.changeBlocks(blocks, player);
        net.minecraft.server.v1_12_R1.World world = ((CraftWorld)player.getWorld()).getHandle();
        EntityPlayer entity = ((CraftPlayer) player).getHandle();
        for(FakeBlock block : blocks) {
            SoundEffectType effectType = breakSounds.get(block.getType());
            world.a(entity, new net.minecraft.server.v1_12_R1.BlockPosition(block.getX(), block.getY(), block.getZ()), effectType.e(), SoundCategory.BLOCKS, (effectType.a() + 1.0F) / 2.0F, effectType.b() * 0.8F);
        }
    }   
    
    private Map<ChangeType, Set<FakeBlock>> getChangesPerPlayer(Player player) {
        return observedWorlds.get(player)
                .entrySet()
                .stream()
                .filter(e -> !((FakeWorldBase)e.getKey()).getChanges().isEmpty())//Ignore empty worlds
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))//From high priority to high
                .flatMap(e -> ((FakeWorldBase)e.getKey()).getChanges().stream())
                .filter(bc -> this.getBlockAt(player, bc.getBlock().getLocation()) == bc.getBlock())
                .collect(Collectors.groupingBy(
                        BlockChange::getType,
                        HashMap::new,
                        Collectors.mapping(
                                BlockChange::getBlock,
                                Collectors.toSet()
                        )
                ));
    }
    
    private static Map<Material, SoundEffectType> breakSounds;
    
    public static FakeWorldManager init(MultiBlockChangeHandler mbchandler) {
        breakSounds = generateBreakSounds();
        FakeWorldManager manager = new FakeWorldManager(mbchandler);
        manager.startBlockCheckLoop();
        for(Player p : Bukkit.getOnlinePlayers()) {
            manager.observedWorlds.put(p, new ConcurrentHashMap<>());
        }
        Bukkit.getPluginManager().registerEvents(manager, VirtualWorld.getInstance());
        return manager;
    }
    
    private static Map<Material, SoundEffectType> generateBreakSounds() {
        Map<Material, SoundEffectType> breakSounds = new HashMap<>();
        for (net.minecraft.server.v1_12_R1.Block block : net.minecraft.server.v1_12_R1.Block.REGISTRY) {
            try {
                Field effectField = net.minecraft.server.v1_12_R1.Block.class.getDeclaredField("stepSound");
                effectField.setAccessible(true);
                SoundEffectType effectType = (SoundEffectType) effectField.get((Object) block);
                breakSounds.put(CraftMagicNumbers.getMaterial((net.minecraft.server.v1_12_R1.Block) block), effectType);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(FakeWorldManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return breakSounds;
    }
}
