/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.api;

import com.spleefleague.virtualworld.FakeWorldManager;
import com.spleefleague.virtualworld.api.implementation.BlockWrapper;
import java.util.Iterator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

/**
 *
 * @author jonas
 */
public class PerspectiveIterator implements Iterator<FakeBlock> {
    
    private final Player perspective;
    private final BlockIterator underlyingIterator;
    private final FakeWorldManager fwm;

    public PerspectiveIterator(Player perspective, BlockIterator underlyingIterator, FakeWorldManager fwm) {
        this.perspective = perspective;
        this.underlyingIterator = underlyingIterator;
        this.fwm = fwm;
    }

    @Override
    public boolean hasNext() {
        return underlyingIterator.hasNext();
    }

    @Override
    public FakeBlock next() {
        Block next = underlyingIterator.next();
        FakeBlock fb = fwm.getBlockAt(perspective, next.getLocation());
        return fb != null ? fb : new BlockWrapper(next);
    }
    
}
