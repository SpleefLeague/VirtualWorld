/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.api.implementation;

import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.api.FakeChunk;
import com.spleefleague.virtualworld.api.FakeWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

/**
 *
 * @author jonas
 */
public class BlockWrapper implements FakeBlock {

    private final Block block;
    
    public BlockWrapper(Block block) {
        this.block = block;
    }
    
    @Override
    public void reset() {
        
    }

    @Override
    public Material getType() {
        return block.getType();
    }

    @Override
    public FakeBlock getRelative(BlockFace face) {
        return new BlockWrapper(block.getRelative(face));
    }

    @Override
    public void setBlockData(BlockData data) {
        block.setBlockData(data);
    }

    @Override
    public void setBlockData(BlockData data, boolean force) {
        block.setBlockData(data);
    }

    @Override
    public BlockData getBlockData() {
        return block.getBlockData();
    }

    @Override
    public FakeWorld getWorld() {
        return null;
    }

    @Override
    public FakeChunk getChunk() {
        return null;
    }

    @Override
    public int getX() {
        return block.getX();
    }

    @Override
    public int getY() {
        return block.getY();
    }

    @Override
    public int getZ() {
        return block.getZ();
    }

    @Override
    public Location getLocation() {
        return block.getLocation();
    }

    @Override
    public Block getHandle() {
        return block;
    }
}
