package com.spleefleague.virtualworld;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 *
 * @author balsfull
 */
public class FakeBlock {
    
    private final FakeChunk chunk;
    private final int x, y, z;
    private Material type;
    private byte data;
    private BlockStateManager stateManager;

    protected FakeBlock(FakeChunk chunk, int x, int y, int z) {
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
        Block handle = getHandle();
        this.type = handle.getType();
        this.data = handle.getData();
    }
    
    public Vector getLocation() {
        return new Vector(x, y, z);
    }

    public FakeChunk getChunk() {
        return chunk;
    }
    
    public FakeWorld getWorld() {
        return chunk.getWorld();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Material getType() {
        return type;
    }

    public byte getData() {
        return data;
    }
    
    public void setType(Material type) {
        setType(type, false);
    }

    public void setType(Material type, boolean force) {
        if(!force && getHandle().getType() == Material.AIR) return;
        this.type = type;
    }
    
    public void setData(byte data) {
        setData(data, false);
    }
    
    public void setData(byte data, boolean force) {
        if(!force && getHandle().getType() == Material.AIR) return;
        this.data = data;
    }
    
    public boolean hasNaturalState() {
        Block handle = getHandle();
        return handle.getType() == type && handle.getData() == data;
    }
    
    private Block getHandle() {
        return chunk.getHandle().getBlock(x, y, z);
    }
}
