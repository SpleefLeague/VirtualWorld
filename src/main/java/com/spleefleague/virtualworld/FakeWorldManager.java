/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld;

import com.spleefleague.virtualworld.BlockChange.ChangeType;
import com.spleefleague.virtualworld.api.FakeBlock;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import org.bukkit.Bukkit;
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
    
    private FakeWorldManager() {
        this.observedWorlds = new ConcurrentHashMap<>();
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
                Collection<FakeBlock> sendChange = new HashSet<>();
                Collection<FakeBlock> sendBreak = new HashSet<>();
                Collection<FakeBlock> sendPlace = new HashSet<>();
                observedWorlds.get(player)
                        .entrySet()
                        .stream()
                        .filter(e -> !e.getKey().getChanges().isEmpty())//Ignore empty worlds
                        .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))//From high priority to high
                        .flatMap(e -> e.getKey().getChanges().stream())
                        .distinct()
                        .map(e -> e.getKey().getChanges())
                        .stream()
                        .forEach(changes -> {
                            Map<ChangeType, List<BlockChange>> grouped = changes
                                    .stream()
                                    .collect(groupingBy(bc -> bc.getType()));
                            sendChange.addAll(grouped
                                    .get(ChangeType.PLUGIN)
                                    .stream().collect(Collectors.toSet()));
                            sendBreak.addAll(grouped
                                    .get(ChangeType.BREAK)
                                    .stream().collect(Collectors.toSet()));
                            sendPlace.addAll(grouped
                                    .get(ChangeType.BREAK)
                                    .stream().collect(Collectors.toSet()));
                            
                        });
                
            }
        }, 0, 1);
    }
    
    public static void init() {
        FakeWorldManager manager = new FakeWorldManager();
        manager.startBlockCheckLoop();
        Bukkit.getPluginManager().registerEvents(manager, VirtualWorld.getInstance());
    }
}
