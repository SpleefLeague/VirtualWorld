package com.spleefleague.virtualworld;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.World;

/**
 *
 * @author balsfull
 */
public class FakeWorld {

    private final Map<Long, FakeChunk> chunks;
    private final World handle;
    private final Area area;
    
    private FakeWorld(World world, Area area) {
        chunks = new HashMap<>();
        this.handle = world;
        this.area = area;
    }
    
    public FakeChunk getChunkAt(int x, int z) {
        if(!area.isInsideX(x * 16) && !area.isInsideX(x * 16 + 15)) return null;
        if(!area.isInsideZ(z * 16) && !area.isInsideZ(z * 16 + 15)) return null;
        long key = getKey(x, z);
        FakeChunk chunk = chunks.get(key);
        if(chunk == null) {
            chunk = new FakeChunk(this, area, x, z);
            chunks.put(key, chunk);
        }
        return chunk;
    }
    
    public FakeBlock getBlockAt(int x, int y, int z) {
        FakeChunk chunk = getChunkAt(x / 16, z / 16);
        if(chunk != null) {
            return chunk.getBlock(x & 15, y, z & 15);
        }
        else {
            return null;
        }
    }
    
    public Collection<FakeBlock> getUsedBlocks() {
        return chunks
                .values()
                .stream()
                .flatMap(fc -> fc.getUsedBlocks().stream())
                .collect(Collectors.toSet());
    }

    public World getHandle() {
        return handle;
    }
    
    private long getKey(int x, int z) {
        long key = x;
        return key << 32 | z;
    }
}
