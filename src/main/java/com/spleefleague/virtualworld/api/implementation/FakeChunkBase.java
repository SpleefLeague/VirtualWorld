package com.spleefleague.virtualworld.api.implementation;

import com.spleefleague.virtualworld.Area;
import com.spleefleague.virtualworld.api.FakeChunk;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.util.Vector;

/**
 *
 * @author balsfull
 */
public class FakeChunkBase implements FakeChunk {
    
    private final int x, z;
    private final Area area;
    private final FakeWorldBase world;
    private final Map<Integer, FakeBlockBase> blocks;

    protected FakeChunkBase(FakeWorldBase world, Area area, int x, int z) {
        this.x = x;
        this.z = z;
        this.world = world;
        this.area = area;
        this.blocks = new HashMap<>();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public FakeWorldBase getWorld() {
        return world;
    }
    
    @Override
    public FakeBlockBase getBlock(int x, int y, int z) {
        if(area != null && !area.isInside(new Vector(this.x * 16 + x, y, this.z * 16 + z))) {
            return null;
        }
        int key = getKey(x, y, z);
        FakeBlockBase block = blocks.get(key);
        if(block == null) {
            block = new FakeBlockBase(this, this.x * 16 + x, y, this.z * 16 + z);
            blocks.put(key, block);
        }
        return block;
    }

    protected FakeBlockBase getBlockRaw(int x, int y, int z) {
        if(area != null && !area.isInside(new Vector(this.x * 16 + x, y, this.z * 16 + z))) {
            return null;
        }
        int key = getKey(x, y, z);
        return blocks.get(key);
    }
    
    @Override
    public Collection<FakeBlockBase> getUsedBlocks() {
        return blocks.values();
    }
    
    protected boolean isEmpty() {
        return blocks.isEmpty();
    }
    
    protected void notifyChange(BlockChange change) {
        world.notifyChange(change);
    }

    @Override
    public Chunk getHandle() {
        return world.getHandle().getChunkAt(x, z);
    }
    
    //[15..0]
    //x: [3..0]
    //y: [11..4]
    //z: [15..12]
    private int getKey(int x, int y, int z) {
        return (0xF & z) << 12 | (0xFF & y) << 4 | (0xF & x);
    }
}
