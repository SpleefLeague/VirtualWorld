package com.spleefleague.virtualworld;

import java.util.Collection;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.util.Vector;

/**
 *
 * @author balsfull
 */
public class FakeChunk {
    
    private final int x, z;
    private final Area area;
    private final FakeWorld world;
    private Map<Integer, FakeBlock> blocks;

    protected FakeChunk(FakeWorld world, Area area, int x, int z) {
        this.x = x;
        this.z = z;
        this.world = world;
        this.area = area;
    }

    public Chunk getHandle() {
        return world.getHandle().getChunkAt(x, z);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public FakeWorld getWorld() {
        return world;
    }
    
    public FakeBlock getBlock(int x, int y, int z) {
        if(!area.isInside(new Vector(this.x * 16 + x, y, this.z * 16 + z))) {
            return null;
        }
        int key = getKey(x, y, z);
        FakeBlock block = blocks.get(key);
        if(block == null) {
            block = new FakeBlock(this, x, y, z);
            blocks.put(key, block);
        }
        return block;
    }
    
    public Collection<FakeBlock> getUsedBlocks() {
        return blocks.values();
    }
    
    //[15..0]
    //x: [3..0]
    //y: [11..4]
    //z: [15..12]
    private int getKey(int x, int y, int z) {
        return (0xF & z) << 12 | (0xFF & y) << 4 | (0xFF & x);
    }
}
