/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld;

import com.spleefleague.virtualworld.api.implementation.BlockChange;
import com.spleefleague.virtualworld.api.implementation.BlockChange.ChangeType;
import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.api.FakeChunk;
import com.spleefleague.virtualworld.api.FakeWorld;
import com.spleefleague.virtualworld.api.implementation.FakeBlockBase;
import com.spleefleague.virtualworld.api.implementation.FakeChunkBase;
import com.spleefleague.virtualworld.api.implementation.FakeWorldBase;
import com.spleefleague.virtualworld.protocol.MultiBlockChangeHandler;
import com.spleefleague.virtualworld.protocol.chunk.BlockPalette;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketListenerPlayOut;
import net.minecraft.server.v1_15_R1.PacketPlayOutWorldEvent;
import net.minecraft.server.v1_15_R1.SoundEffectType;
import net.minecraft.server.v1_15_R1.SoundCategory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.minecraft.server.v1_15_R1.BlockPosition;

/**
 *
 * @author jonas
 */
public class FakeWorldManager implements Listener {
    
    private final Map<UUID, Map<FakeWorld, Integer>> observedWorlds;
    private final MultiBlockChangeHandler mbcHandler;
    
    private FakeWorldManager(MultiBlockChangeHandler mbcHandler) {
        this.observedWorlds = new ConcurrentHashMap<>();
        this.mbcHandler = mbcHandler;
    }
    
    public Set<Player> getPrimarySubscribers(FakeBlock block) {
        return observedWorlds.entrySet()
                    .stream()
                    .filter(e -> this.getWorldAt(e.getKey(), block.getLocation()) == block.getWorld())
                    .map(Entry::getKey)
                    .map(Bukkit::getPlayer)
                    .collect(Collectors.toSet());
    }
    
    public Set<Player> getSubscribers(FakeWorld world) {
        return observedWorlds.entrySet()
                    .stream()
                    .filter(e -> e.getValue().keySet().contains(world))
                    .map(Entry::getKey)
                    .map(Bukkit::getPlayer)
                    .collect(Collectors.toSet());
    }
    
    public Collection<FakeBlock> getBlocksInChunk(Player player, int x, int z) {
        return observedWorlds.get(player.getUniqueId())
                .entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))//From high priority to low
                .map(e -> ((FakeWorldBase)e.getKey()).getChunkAtRaw(x, z))
                .filter(c -> c != null)
                .flatMap(c -> c.getUsedBlocks().stream())
                /*This might need extra verification*/
                .filter(distinctByKey(fb -> (0xFF & (long)fb.getY()) | (0xFFFFFFF & (long)fb.getX() << 8) | (0xFFFFFFF & (long)fb.getZ()) << 36))
                .collect(Collectors.toSet());
    }
    
    private static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.put(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
    
    public FakeWorld getWorldAt(Player player, Location l) {
        return this.getWorldAt(player.getUniqueId(), l);
    }
    
    public FakeWorld getWorldAt(UUID playerId, Location l) {
        return getWorldAt(playerId, l, a -> true);
    }
    
    public FakeWorld getWorldAt(UUID playerId, Location l, Predicate<FakeWorld> predicate) {
        return observedWorlds.get(playerId)
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getHandle() == l.getWorld())
                .filter(fw -> fw.getKey().getArea() == null || fw.getKey().getArea().isInside(l.toVector()))
                .filter(fw -> predicate.test(fw.getKey()))
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                //.max((e1, e2) -> Integer.compare(e1.getValue(), e2.getValue()))
                .map(Entry::getKey)
                .findFirst()
                .orElse(null);
    }
    
    public FakeBlock getBlockAt(Player player, Location l) {
        return getBlockAt(player.getUniqueId(), l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }
    
    public FakeBlock getBlockAt(UUID playerId, Location l) {
        return getBlockAt(playerId, l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }
    
    public FakeBlock getBlockAt(Player player, World world, int x, int y, int z) {
        return this.getBlockAt(player.getUniqueId(), world, x, y, z);
    }
    
    public FakeBlock getBlockAt(UUID playerId, World world, int x, int y, int z) {
        FakeBlock fb =  observedWorlds.get(playerId)
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getHandle() == world)
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                //.max((e1, e2) -> Integer.compare(e1.getValue(), e2.getValue()))
                .map(e -> e.getKey())
                .map(fw -> ((FakeWorldBase)fw).getBlockAtRaw(x, y, z))
                .filter(o -> o != null)
                .findFirst()
                .orElse(null);
        return fb;
    }

    public boolean isFakeBlock(Player p, World world, int x, int y, int z) {
        return observedWorlds.get(p.getUniqueId())
                .keySet()
                .stream()
                .filter(e -> e.getHandle() == world)
                .map(fw -> ((FakeWorldBase)fw).getBlockAtRaw(x, y, z))
                .anyMatch(o -> o != null);
    }

    public boolean isFakeChunk(Player p, World world, int x, int z) {
        return observedWorlds.get(p.getUniqueId())
                .keySet()
                .stream()
                .filter(e -> e.getHandle() == world)
                .map(fw -> ((FakeWorldBase)fw).getChunkAtRaw(x, z))
                .anyMatch(o -> o != null);
    }

    public List<FakeChunk> getChunkList(Player p, World world, int x, int z) {
        return observedWorlds.get(p.getUniqueId())
                .keySet()
                .stream()
                .filter(e -> e.getHandle() == world)
                .map(fw -> ((FakeWorldBase)fw).getChunkAtRaw(x, z))
                .filter(o -> o != null)
                .collect(Collectors.toList());
    }
    
    public void addWorld(Player player, FakeWorld world, int priority) {
        Map<FakeWorld, Integer> worlds = observedWorlds.get(player.getUniqueId());
        if(!worlds.containsKey(world)) {
            worlds.put(world, priority);
            Collection<FakeBlock> newChanges = ((FakeWorldBase)world).getUsedBlocks()
                    .stream()
                    .filter(fb -> getBlockAt(player, fb.getLocation()).getWorld() == world)
                    .collect(Collectors.toSet());
            sendDirectRaw(player, newChanges);
        }
    }
    
    public void removeWorld(FakeWorld world) {
        observedWorlds.keySet().forEach(uuid -> removeWorld(Bukkit.getPlayer(uuid), world));
    }
    
    public void removeWorld(Player player, FakeWorld world) {
        Map<FakeWorld, Integer> worlds = observedWorlds.get(player.getUniqueId());
        if(worlds.containsKey(world)) {
            worlds.remove(world);
            Collection<FakeBlock> newChanges =((FakeWorldBase)world).getUsedBlocks()
                    .stream()
                    .map(fb -> {
                        FakeBlock replacement = getBlockAt(player, fb.getLocation());
                        if(replacement == null) {
                            //Fake block only to be used to hold location + material.
                            replacement = new FakeBlockBase((FakeChunkBase)world.getChunkAt(fb.getX() >> 4, fb.getZ() >> 4), fb.getX(), fb.getY(), fb.getZ());
                            ((FakeBlockBase)replacement)._setBlockData(replacement.getHandle().getBlockData());
                            ((FakeBlockBase)replacement)._setType(replacement.getHandle().getType());
                        }
                        return replacement;
                    })
                    .collect(Collectors.toSet());
            sendDirectRaw(player, newChanges);
        }
    }
    
    public FakeWorld createWorld(World world) {
        return createWorld(world, null);
    }
    
    public FakeWorld createWorld(World world, Area area) {
        return createWorld(world, area, false);
    }
    
    public FakeWorld createWorld(World world, Area area, boolean deepCopy) {
        FakeWorldBase fwb = new FakeWorldBase(world, area);
        if(area == null || !deepCopy) return fwb;
        for (int x = area.getLow().getBlockX(); x <= area.getHigh().getBlockX(); x++) {
            for (int y = area.getLow().getBlockY(); y <= area.getHigh().getBlockY(); y++) {
                for (int z = area.getLow().getBlockZ(); z <= area.getHigh().getBlockZ(); z++) {
                    org.bukkit.block.Block block = world.getBlockAt(x, y, z);
                    fwb.getBlockAt(x, y, z)._setType(block.getType());
                    fwb.getBlockAt(x, y, z)._setBlockData(block.getBlockData());
                }
            }
        }
        return fwb;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        observedWorlds.remove(event.getPlayer().getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        observedWorlds.put(event.getPlayer().getUniqueId(), new ConcurrentHashMap<>());
    }
    
    private void startBlockCheckLoop() {
        Bukkit.getScheduler().runTaskTimer(VirtualWorld.getInstance(), () -> {
            Map<FakeBlock, FakeBlockBase> breakData = new HashMap<>();
            for(UUID playerId : observedWorlds.keySet()) {
                Map<ChangeType, Set<BlockChange>> changes = getChangesPerPlayer(playerId);
                Player player = Bukkit.getPlayer(playerId);
                sendDirect(player, changes.get(ChangeType.PLUGIN));
                sendPlace(player, changes.get(ChangeType.PLACE));
                sendBreak(player, changes.get(ChangeType.BREAK));
            }
            observedWorlds.values()
                    .stream()
                    .flatMap(m -> m.keySet().stream())
                    .forEach(fw -> {
                        ((FakeWorldBase)fw).clearChanges();
                            });
        }, 0, 1);
    }
    
    private void sendDirect(Player player, Collection<BlockChange> changes) {
        if(changes == null || changes.isEmpty()) return;
        sendDirectRaw(player, changes
                .stream()
                .map(bc -> bc.getBlock())
                .collect(Collectors.toSet()));
    }
    
    private void sendDirectRaw(Player player, Collection<? extends FakeBlock> changes) {
        if(changes == null || changes.isEmpty()) return;
        mbcHandler.changeBlocks(changes, player);
    }
    
    private void sendBreak(Player player, Collection<BlockChange> changes) {
        if(changes == null || changes.isEmpty()) return;
        final int packetId = 2001;
        for(BlockChange change : changes) {
            if(change.getCause() == player) continue;
            FakeBlock block = change.getBlock();
            BlockData prevState = change.getPreviousState();
            if(prevState == null) continue;
            BlockPosition nmsPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
            int blockId = BlockPalette.blockDataToId(prevState);
            PacketPlayOutWorldEvent ppowe = new PacketPlayOutWorldEvent(packetId, nmsPosition, blockId, false);
            sendPacket(player, ppowe);
        }
        mbcHandler.changeBlocks(changes
                .stream()
                .map(bc -> bc.getBlock())
                .collect(Collectors.toSet()), player);
    }
    
    private void sendPlace(Player player, Collection<BlockChange> changes) {
        if(changes == null || changes.isEmpty()) return;
        mbcHandler.changeBlocks(changes
                .stream()
                .map(bc -> bc.getBlock())
                .collect(Collectors.toSet()), player);
        net.minecraft.server.v1_15_R1.World world = ((CraftWorld)player.getWorld()).getHandle();
        EntityPlayer entity = ((CraftPlayer) player).getHandle();
        for(BlockChange change : changes) {
            if(change.getCause() == player) continue;
            FakeBlock block = change.getBlock();
            SoundEffectType effectType = breakSounds.get(block.getType());
            BlockPosition nmsPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
            world.playSound(entity, nmsPosition, effectType.e(), SoundCategory.BLOCKS, (effectType.a() + 1.0F) / 2.0F, effectType.b() * 0.8F);
        }
    }   
    
    private Map<ChangeType, Set<BlockChange>> getChangesPerPlayer(UUID playerId) {
        return observedWorlds.get(playerId)
                .entrySet()
                .stream()
                .filter(e -> !((FakeWorldBase)e.getKey()).getChanges().isEmpty())//Ignore empty worlds
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))//From high priority to high
                .flatMap(e -> ((FakeWorldBase)e.getKey()).getChanges().stream())
                .filter(bc -> this.getBlockAt(playerId, bc.getBlock().getLocation()) == bc.getBlock())
                .collect(Collectors.groupingBy(
                        BlockChange::getType,
                        HashMap::new,
                        Collectors.mapping(
                                Function.identity(),
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
            manager.observedWorlds.put(p.getUniqueId(), new ConcurrentHashMap<>());
        }
        Bukkit.getPluginManager().registerEvents(manager, VirtualWorld.getInstance());
        return manager;
    }
    
    private static Map<Material, SoundEffectType> generateBreakSounds() {
        Map<Material, SoundEffectType> breakSounds = new HashMap<>();
        Iterator<IBlockData> iter = Block.REGISTRY_ID.iterator();
        while(iter.hasNext()) {
            net.minecraft.server.v1_15_R1.Block block = iter.next().getBlock();
            try {
                Field effectField = net.minecraft.server.v1_15_R1.Block.class.getDeclaredField("stepSound");
                effectField.setAccessible(true);
                SoundEffectType effectType = (SoundEffectType) effectField.get((Object) block);
                breakSounds.put(CraftMagicNumbers.getMaterial((net.minecraft.server.v1_15_R1.Block) block), effectType);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(FakeWorldManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return breakSounds;
    }
    
    private void sendPacket(Player player, Packet<PacketListenerPlayOut> packet) {
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }
}
