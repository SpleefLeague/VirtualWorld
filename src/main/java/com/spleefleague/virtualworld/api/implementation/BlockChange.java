/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.api.implementation;

import java.util.Objects;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

/**
 *
 * @author jonas
 */
public class BlockChange {
    
    private final FakeBlockBase block;
    private final ChangeType type;
    private final BlockData previousState;
    private final Player cause;

    public BlockChange(FakeBlockBase block, ChangeType type, BlockData previous, Player cause) {
        this.block = block;
        this.type = type;
        this.cause = cause;
        this.previousState = previous;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.block);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BlockChange other = (BlockChange) obj;
        if (!Objects.equals(this.block, other.block)) {
            return false;
        }
        return true;
    }
    
    public FakeBlockBase getBlock() {
        return block;
    }

    public ChangeType getType() {
        return type;
    }

    public BlockData getPreviousState() {
        return previousState;
    }

    public Player getCause() {
        return cause;
    }
    
    public static enum ChangeType {
        PLUGIN,
        BREAK,
        PLACE;
    }
}
