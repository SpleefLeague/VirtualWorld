/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.api;

import java.util.Collection;
import org.bukkit.World;

/**
 *
 * @author jonas
 */
public interface FakeWorld {
    
    FakeChunk getChunkAt(int x, int z);
    FakeBlock getBlockAt(int x, int y, int z);
    Collection<? extends FakeBlock> getUsedBlock(); 
    World getHandle();
}
