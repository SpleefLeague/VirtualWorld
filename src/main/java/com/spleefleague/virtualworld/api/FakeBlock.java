/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 *
 * @author jonas
 */
public interface FakeBlock {
    
    Material getType();
    void setType(Material type);
    void setType(Material type, boolean force);
    byte getData();
    void setData(byte data);
    void setData(byte data, boolean force);
    FakeWorld getWorld();
    FakeChunk getChunk();
    int getX();
    int getY();
    int getZ();
    Location getLocation();
    Block getHandle();
}
