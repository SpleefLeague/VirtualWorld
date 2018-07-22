/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

/**
 *
 * @author jonas
 */
public interface FakeBlock {
    
    Material getType();
    default void setType(Material type) {
        setBlockData(type.createBlockData());
    }
    default void setType(Material type, boolean force) {
        setBlockData(type.createBlockData(), force);
    }
    void setBlockData(BlockData data);
    void setBlockData(BlockData data, boolean force);
    BlockData getBlockData();
    FakeWorld getWorld();
    FakeChunk getChunk();
    int getX();
    int getY();
    int getZ();
    Location getLocation();
    Block getHandle();
}
