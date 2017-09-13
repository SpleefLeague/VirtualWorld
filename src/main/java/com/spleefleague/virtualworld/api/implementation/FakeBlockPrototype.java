package com.spleefleague.virtualworld.api.implementation;

import com.spleefleague.virtualworld.api.FakeBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 *
 * @author balsfull
 */
public class FakeBlockPrototype implements FakeBlock {

    private final FakeChunkBase chunk;
    private final int x, y, z;
    private Material type;
    private byte data;

    public FakeBlockPrototype(FakeChunkBase chunk, int x, int y, int z) {
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
        Block handle = getHandle();
        this.type = handle.getType();
        this.data = handle.getData();
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

    @Override
    public Material getType() {
        return type;
    }

    @Override
    public byte getData() {
        return data;
    }
    
    @Override
    public void setType(Material type) {
        setType(type, false);
    }

    @Override
    public void setType(Material type, boolean force) {
        this.type = type;
    }
    
    @Override
    public void setData(byte data) {
        setData(data, false);
    }
    
    @Override
    public void setData(byte data, boolean force) {
        this.data = data;
    }
    
    public boolean hasNaturalState() {
        Block handle = getHandle();
        return handle.getType() == type && handle.getData() == data;
    }
    
    @Override
    public Block getHandle() {
        return chunk.getHandle().getBlock(x, y, z);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.x;
        hash = 37 * hash + this.y;
        hash = 37 * hash + this.z;
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
        final FakeBlockPrototype other = (FakeBlockPrototype) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.z != other.z) {
            return false;
        }
        return true;
    }
}
