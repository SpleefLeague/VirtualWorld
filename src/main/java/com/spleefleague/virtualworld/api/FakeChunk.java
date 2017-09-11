/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.api;

import java.util.Collection;
import org.bukkit.Chunk;

/**
 *
 * @author jonas
 */
public interface FakeChunk {
    
    int getX();
    int getZ();
    FakeWorld getWorld();
    FakeBlock getBlock(int x, int y, int z);
    Collection<? extends FakeBlock> getUsedBlocks();
    Chunk getHandle();
}
