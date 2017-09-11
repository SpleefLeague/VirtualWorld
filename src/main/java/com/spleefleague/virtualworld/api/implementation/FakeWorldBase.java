package com.spleefleague.virtualworld.api.implementation;

import com.spleefleague.virtualworld.Area;
import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.api.FakeWorld;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.World;

/**
 *
 * @author balsfull
 */
public class FakeWorldBase implements FakeWorld {

    private final Map<Long, FakeChunkBase> chunks;
    private final World handle;
    private final Area area;
    private final Collection<BlockChange> changes;
    
    private FakeWorldBase(World world, Area area) {
        this.chunks = new HashMap<>();
        this.changes = new HashSet<>();
        this.handle = world;
        this.area = area;
    }
    
    @Override
    public FakeChunkBase getChunkAt(int x, int z) {
        if(!area.isInsideX(x * 16) && !area.isInsideX(x * 16 + 15)) return null;
        if(!area.isInsideZ(z * 16) && !area.isInsideZ(z * 16 + 15)) return null;
        long key = getKey(x, z);
        FakeChunkBase chunk = chunks.get(key);
        if(chunk == null) {
            chunk = new FakeChunkBase(this, area, x, z);
            chunks.put(key, chunk);
        }
        return chunk;
    }
    
    public FakeChunkBase getChunkAtRaw(int x, int z) {
        if(!area.isInsideX(x * 16) && !area.isInsideX(x * 16 + 15)) return null;
        if(!area.isInsideZ(z * 16) && !area.isInsideZ(z * 16 + 15)) return null;
        long key = getKey(x, z);
        return chunks.get(key);
    }
    
    @Override
    public FakeBlockBase getBlockAt(int x, int y, int z) {
        FakeChunkBase chunk = getChunkAt(x / 16, z / 16);
        if(chunk != null) {
            return chunk.getBlock(x & 15, y, z & 15);
        }
        else {
            return null;
        }
    }
    
    public FakeBlockBase getBlockAtRaw(int x, int y, int z) {
        FakeChunkBase chunk = getChunkAtRaw(x / 16, z / 16);
        if(chunk != null) {
            return chunk.getBlockRaw(x & 15, y, z & 15);
        }
        else {
            return null;
        }
    }
    
    public Collection<FakeBlockBase> getUsedBlocks() {
        return chunks
                .values()
                .stream()
                .flatMap(fc -> fc.getUsedBlocks().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public World getHandle() {
        return handle;
    }
    
    public Collection<BlockChange> getChanges() {
        return changes;
    }
    
    public void clearChanges() {
        changes.clear();
    }

    protected void notifyChange(BlockChange change) {
        FakeChunkBase chunk = change.getBlock().getChunk();
        if(chunk.isEmpty()) {
            chunks.remove(chunk.getX(), chunk.getZ());
        }
        changes.add(change);
    }
    
    private long getKey(int x, int z) {
        long key = x;
        return key << 32 | z;
    }

    @Override
    public Collection<? extends FakeBlock> getUsedBlock() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
