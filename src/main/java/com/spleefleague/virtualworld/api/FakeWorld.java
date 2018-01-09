/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.api;

import com.spleefleague.virtualworld.Area;
import java.util.Collection;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 *
 * @author jonas
 */
public interface FakeWorld {
    
    FakeChunk getChunkAt(int x, int z);
    FakeBlock getBlockAt(int x, int y, int z);
    FakeBlock getBlockAt(Location loc);
    FakeBlock getBlockAt(Vector vec);
    Collection<? extends FakeBlock> getUsedBlocks(); 
    World getHandle();
    Area getArea();
    
    void playEffect(Location lctn, Effect effect, int i);
    <T extends Object> void playEffect(Location lctn, Effect effect, T t);
}
