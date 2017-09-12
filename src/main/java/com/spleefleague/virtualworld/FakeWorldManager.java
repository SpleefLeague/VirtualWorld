/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld;

import com.spleefleague.virtualworld.api.implementation.BlockChange;
import com.spleefleague.virtualworld.api.implementation.FakeBlockBase;
import com.spleefleague.virtualworld.api.implementation.FakeWorldBase;
import com.spleefleague.virtualworld.api.implementation.BlockChange.ChangeType;
import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.api.FakeWorld;
import com.spleefleague.virtualworld.protocol.MultiBlockChangeHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
    
    private final Map<Player, Map<FakeWorldBase, Integer>> observedWorlds;
    private final MultiBlockChangeHandler mbchandler;
    
    private FakeWorldManager(MultiBlockChangeHandler mbchandler) {
        this.observedWorlds = new ConcurrentHashMap<>();
        this.mbchandler = mbchandler;
    }
    
    public Collection<FakeBlockBase> getBlocksInChunk(Player player, int x, int z) {
        return observedWorlds.get(player)
                .entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))//From high priority to high
                .map(e -> e.getKey().getChunkAtRaw(x, z))
                .filter(c -> c != null)
                .flatMap(c -> c.getUsedBlocks().stream())
                .distinct()
                .collect(Collectors.toSet());
    }
    
    public FakeWorldBase getWorldAt(Player player, World world, Location l) {
        return observedWorlds.get(player)
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getHandle() == world)
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .map(e -> e.getKey())
                .filter(fw -> fw.getArea().isInside(l.toVector()))
                .findFirst()
                .orElse(null);
                
    }
    
    public FakeBlockBase getBlockAt(Player player, Location l) {
        return getBlockAt(player, l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }
    
    public FakeBlockBase getBlockAt(Player player, World world, int x, int y, int z) {
        return observedWorlds.get(player)
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getHandle() == world)
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .map(e -> e.getKey())
                .findFirst()
                .map(fw -> fw.getBlockAtRaw(x, y, z))
                .orElse(null);
    }
    
    public void addWorld(Player player, FakeWorldBase world, int priority) {
        Map<FakeWorldBase, Integer> worlds = observedWorlds.get(player);
        if(!worlds.containsKey(world)) {
            worlds.put(world, priority);
            Collection<FakeBlockBase> blocks = world.getUsedBlocks();
            //Send changes
        }
    }
    
    public void removeWorld(Player player, FakeWorldBase world) {
        Map<FakeWorldBase, Integer> worlds = observedWorlds.get(player);
        if(!worlds.containsKey(world)) {
            worlds.remove(world);
            Collection<FakeBlockBase> blocks = world.getUsedBlocks();
            //Send changes
        }
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
        Bukkit.getScheduler().runTaskTimerAsynchronously(VirtualWorld.getInstance(), () -> {
            for(Player player : observedWorlds.keySet()) {
                Map<ChangeType, Set<FakeBlock>> changes = getChangesPerPlayer(player);
                //Send changes
            }
            observedWorlds.values()
                    .stream()
                    .flatMap(m -> m.keySet().stream())
                    .forEach(fw -> fw.getChanges().clear());
        }, 0, 1);
    }
    
    private Map<ChangeType, Set<FakeBlock>> getChangesPerPlayer(Player player) {
        return observedWorlds.get(player)
                .entrySet()
                .stream()
                .filter(e -> !e.getKey().getChanges().isEmpty())//Ignore empty worlds
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))//From high priority to high
                .flatMap(e -> e.getKey().getChanges().stream())
                .distinct()//FakeBlock.equals is true if the coordinates are the same
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
        Bukkit.getPluginManager().registerEvents(manager, VirtualWorld.getInstance());
        return manager;
    }
}
