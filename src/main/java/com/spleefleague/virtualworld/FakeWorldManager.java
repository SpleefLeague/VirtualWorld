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
import com.spleefleague.virtualworld.api.implementation.FakeWorldBase;
import com.spleefleague.virtualworld.protocol.MultiBlockChangeHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
                .filter(fw -> fw.getArea().isInside(l.toVector()))
                .findFirst()
                .orElse(null);
                
    }
    
    public FakeBlock getBlockAt(Player player, Location l) {
        return getBlockAt(player, l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }
    
    public FakeBlock getBlockAt(Player player, World world, int x, int y, int z) {
        System.out.println("Getting " + x + " " + y + " " + z + " " + player + " " + world);
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
        System.out.println(fb);
        return fb;
    }
    
    public void addWorld(Player player, FakeWorld world, int priority) {
        Map<FakeWorld, Integer> worlds = observedWorlds.get(player);
        if(!worlds.containsKey(world)) {
            worlds.put(world, priority);
            Collection<FakeBlockBase> blocks = ((FakeWorldBase)world).getUsedBlocks();
            //Send changes
        }
    }
    
    public void removeWorld(Player player, FakeWorld world) {
        Map<FakeWorld, Integer> worlds = observedWorlds.get(player);
        if(!worlds.containsKey(world)) {
            worlds.remove(world);
            Collection<FakeBlockBase> blocks = ((FakeWorldBase)world).getUsedBlocks();
            //Send changes
        }
    }
    
    public FakeWorld createWorld(World world, Area area) {
        return new FakeWorldBase(world, area);
    }
    
    @EventHandler
    public void onJoin(PlayerQuitEvent event) {
        observedWorlds.remove(event.getPlayer());
    }
    
    @EventHandler
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
    
    private void sendDirect(Player player, Set<FakeBlock> blocks) {
        if(blocks == null || blocks.isEmpty()) return;
        mbchandler.changeBlocks(blocks, player);
    }
    
    private void sendBreak(Player player, Set<FakeBlock> blocks) {
        if(blocks == null || blocks.isEmpty()) return;
        mbchandler.changeBlocks(blocks, player);
    }
    
    private void sendPlace(Player player, Set<FakeBlock> blocks) {
        if(blocks == null || blocks.isEmpty()) return;
        mbchandler.changeBlocks(blocks, player);
    }
    
    private Map<ChangeType, Set<FakeBlock>> getChangesPerPlayer(Player player) {
        return observedWorlds.get(player)
                .entrySet()
                .stream()
                .filter(e -> !((FakeWorldBase)e.getKey()).getChanges().isEmpty())//Ignore empty worlds
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))//From high priority to high
                .flatMap(e -> ((FakeWorldBase)e.getKey()).getChanges().stream())
                //.distinct()
                .filter(bc -> {
                    System.out.println(this.getBlockAt(player, bc.getBlock().getLocation()));
                    System.out.println(bc.getBlock());
                    System.out.println(bc.getBlock().getWorld().getBlockAtRaw(bc.getBlock().getX(), bc.getBlock().getY(), bc.getBlock().getZ()));
                    return this.getBlockAt(player, bc.getBlock().getLocation()) == bc.getBlock();
                            
                            })//Makes the previous distinct redundant, but is slower
                .collect(Collectors.groupingBy(
                        BlockChange::getType,
                        HashMap::new,
                        Collectors.mapping(
                                BlockChange::getBlock,
                                Collectors.toSet()
                        )
                ));
    }
    
    public static FakeWorldManager init(MultiBlockChangeHandler mbchandler) {
        FakeWorldManager manager = new FakeWorldManager(mbchandler);
        manager.startBlockCheckLoop();
        for(Player p : Bukkit.getOnlinePlayers()) {
            manager.observedWorlds.put(p, new ConcurrentHashMap<>());
        }
        Bukkit.getPluginManager().registerEvents(manager, VirtualWorld.getInstance());
        return manager;
    }
}
