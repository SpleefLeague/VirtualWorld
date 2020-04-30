/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.api;

import com.spleefleague.virtualworld.FakeWorldManager;
import java.util.Iterator;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;

/**
 *
 * @author jonas
 */
public class FakeWorldBlockIterator implements Iterator<FakeBlock> {
    
    private final FakeWorld world;
    private final BlockIterator underlyingIterator;

    public FakeWorldBlockIterator(FakeWorld world, BlockIterator underlyingIterator) {
        this.world = world;
        this.underlyingIterator = underlyingIterator;
    }

    @Override
    public boolean hasNext() {
        return underlyingIterator.hasNext();
    }

    @Override
    public FakeBlock next() {
        Block next = underlyingIterator.next();
        return world.getBlockAt(next.getLocation());
    }
    
}
