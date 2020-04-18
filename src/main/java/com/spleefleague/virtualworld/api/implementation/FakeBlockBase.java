package com.spleefleague.virtualworld.api.implementation;

import com.spleefleague.virtualworld.api.implementation.BlockChange.ChangeType;
import com.spleefleague.virtualworld.api.FakeBlock;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author balsfull
 */
public class FakeBlockBase implements FakeBlock {
    
    private final FakeChunkBase chunk;
    private final int x, y, z;
    private final BlockData blockdata;

    public FakeBlockBase(FakeChunkBase chunk, int x, int y, int z) {
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
        if(chunk != null) {
            Block handle = getHandle();
            blockdata = new BlockData(handle.getType(), (byte) 0);
        }
        else {
            blockdata = null;
        }
    }
    
    @Override
    public Location getLocation() {
        return new Location(getWorld().getHandle(), x, y, z);
    }

    @Override
    public FakeChunkBase getChunk() {
        return chunk;
    }
    
    @Override
    public FakeWorldBase getWorld() {
        return chunk.getWorld();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    public BlockData getBlockdata() {
        return blockdata;
    }

    @Override
    public Material getType() {
        return blockdata.getType();
    }

    @Override
    public byte getData() {
        return blockdata.getData();
    }
    
    @Override
    public void setType(Material type) {
        setType(type, false);
    }

    @Override
    public void setType(Material type, boolean force) {
        if(!force && getHandle().getType() != Material.AIR) return;
        BlockData oldState = blockdata.copy();
        _setType(type);
        registerChanged(ChangeType.PLUGIN, oldState, null);
    }
    
    public void _setType(Material type) {
        this.blockdata.setType(type);
    }
    
    @Override
    public void setData(byte data) {
        setData(data, false);
    }
    
    @Override
    public void setData(byte data, boolean force) {
        if(!force && getHandle().getType() != Material.AIR) return;
        BlockData oldState = blockdata.copy();
        _setData(data);
        registerChanged(ChangeType.PLUGIN, oldState, null);
    }
    
    public void _setData(byte data) {
        this.blockdata.setData(data);
    }
    
    public boolean hasNaturalState() {
        Block handle = getHandle();
        return handle.getType() == blockdata.getType() && handle.getData() == blockdata.getData();
    }
    
    @Override
    public Block getHandle() {
        return chunk.getHandle().getBlock(x & 15, y, z & 15);
    }

    public void registerChanged(ChangeType type, BlockData oldState, Player cause) {
        this.chunk.notifyChange(new BlockChange(this, type, oldState, cause));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.chunk);
        hash = 61 * hash + this.x;
        hash = 61 * hash + this.y;
        hash = 61 * hash + this.z;
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
        final FakeBlockBase other = (FakeBlockBase) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.z != other.z) {
            return false;
        }
        if (!Objects.equals(this.chunk, other.chunk)) {
            return false;
        }
        return true;
    }
}
