package com.spleefleague.virtualworld;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.partitioningBy;
import javafx.util.Pair;
import org.bukkit.util.Vector;

/**
 *
 * @author balsfull
 */
public class BlockStateManager {
    
    private final Map<Vector, StateChange> stateChanges;
    private final Map<FakeWorld, Integer> knownWorlds;
    
    public BlockStateManager() {
        this.stateChanges = new HashMap<>();
        this.knownWorlds = new HashMap<>();
    }
    
    public void registerWorld(FakeWorld world, int priority) {
        this.knownWorlds.put(world, priority);
    }
    
    /**
     * This method removes one world, and calculates the blocks that require changing
     * so the player sees a consistent representation of the remaining worlds.
     * @param world The removed world
     * @param update If the player should receive an updated version of the world
     */
    public void unregisterWorld(FakeWorld world, boolean update) {
        if(!knownWorlds.containsKey(world)) return;
        int oldPriority = knownWorlds.get(world);
        this.knownWorlds.remove(world);
        if(!update) return;
//        Map<Boolean, List<FakeWorld>> partitioned = knownWorlds
//                .keySet()
//                .parallelStream()
//                .collect(partitioningBy(e -> knownWorlds.get(e) < oldPriority));
//        Collection<FakeBlock> correctBlocks = partitioned.get(false)
//                .parallelStream()
//                .flatMap(fw -> fw.getUsedBlocks().stream())
//                .collect(Collectors.toSet());
//        Map<Vector, FakeBlock> potentialReplacements = partitioned.get(true)
//                .stream()
//                .map(fw -> new Pair<>(fw, knownWorlds.get(fw)))
//                .flatMap(p -> p.getKey()
//                        .getUsedBlocks()
//                        .stream()
//                        .map(fb -> new StateChange(fb, p.getValue()))
//                )
//                .collect(Collectors.toMap(
//                        sc -> sc.block.getLocation(),
//                        sc -> sc,
//                        (sc1, sc2) -> sc1.priority > sc2.priority ? sc1 : sc2
//                ))
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().block));
//        world.getUsedBlocks()
//                .stream()
//                .filter(fb -> correctBlocks.contains(fb))
//                .map(fb -> {
//                    FakeBlock replace = potentialReplacements.get(fb.getLocation());
//                    if(replace != null) {
//                        
//                    }
//                    return replace;
//                })
//                .forEach(fb -> potentialReplacements.remove(fb.getLocation()));
//        potentialReplacements.values()
//                .stream()
//                .forEach(e -> notifyStateChange(e));
    }

    public void notifyStateChange(FakeBlock block) {
        FakeWorld world = block.getWorld();
        if(!knownWorlds.containsKey(world)) return;
        int priority = knownWorlds.get(world);
        Vector v = block.getLocation();
        StateChange previous = stateChanges.get(v);
        if(previous != null) {
            if(previous.priority < priority) {
                previous.priority = priority;
                previous.block = block;
            }
        }
        else {
            stateChanges.put(v, new StateChange(block, priority));
        }
    }
    
    public Collection<FakeBlock> getStateChanges() {
        return stateChanges
                .values()
                .stream()
                .map(sc -> sc.block)
                .collect(Collectors.toSet());
    }
    
    public void clear() {
        stateChanges.clear();
    }
    
    private class StateChange {
        private FakeBlock block;
        private int priority;

        public StateChange(FakeBlock block, int priority) {
            this.block = block;
            this.priority = priority;
        }
    }
}
