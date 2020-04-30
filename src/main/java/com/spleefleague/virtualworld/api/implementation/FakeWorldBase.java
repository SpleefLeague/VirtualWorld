package com.spleefleague.virtualworld.api.implementation;

import com.spleefleague.virtualworld.Area;
import com.spleefleague.virtualworld.FakeWorldManager;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.FakeWorld;
import com.spleefleague.virtualworld.api.implementation.BlockChange.ChangeType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author balsfull
 */
public class FakeWorldBase implements FakeWorld {

    private final Map<Long, FakeChunkBase> chunks;
    private final World handle;
    private final Area area;
    private final Set<BlockChange> changes;
    private final FakeWorldManager fwm;
    private boolean fastEditing;
    private boolean allowBuilding = false;
    
    public FakeWorldBase(World world, Area area) {
        this.chunks = new HashMap<>();
        this.changes = new HashSet<>();
        this.handle = world;
        this.area = area;
        this.fwm = VirtualWorld.getInstance().getFakeWorldManager();
    }

    @Override
    public boolean isAllowBuilding() {
        return allowBuilding;
    }
    
    @Override
    public void setAllowBuilding(boolean allowBuilding) {
        this.allowBuilding = allowBuilding;
    }
    
    @Override
    public FakeChunkBase getChunkAt(int x, int z) {
        if(!isInside(x, z)) return null;
        long key = getKey(x, z);
        FakeChunkBase chunk = chunks.get(key);
        if(chunk == null) {
            chunk = new FakeChunkBase(this, area, x, z);
            chunks.put(key, chunk);
        }
        return chunk;
    }
    
    public FakeChunkBase getChunkAtRaw(int x, int z) {
        if(!isInside(x, z)) return null;
        long key = getKey(x, z);
        return chunks.get(key);
    }
    
    private boolean isInside(int x, int z) {
        if(area != null) {
            if(!area.isInsideX(x * 16) && !area.isInsideX(x * 16 + 15)) {
                if(x < 0) {
                    if(area.getHigh().getX() < x * 16 || area.getLow().getX() > x * 16 + 15) {
                        return false;
                    }
                }
                else {
                    if(area.getLow().getX() < x * 16 || area.getHigh().getX() > x * 16 + 15) {
                        return false;
                    }
                }
            }
            if(!area.isInsideZ(z * 16) && !area.isInsideZ(z * 16 + 15)) {
                if(z < 0) {
                    if(area.getHigh().getZ() < z * 16 || area.getLow().getZ() > z * 16 + 15) {
                        return false;
                    }
                }
                else {
                    if(area.getLow().getZ() < z * 16 || area.getHigh().getZ() > z * 16 + 15) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    @Override
    public FakeBlockBase getBlockAt(int x, int y, int z) {
        FakeChunkBase chunk = getChunkAt(x >> 4, z >> 4);
        if(chunk != null) {
            return chunk.getBlock(x & 15, y, z & 15);
        }
        else {
            return null;
        }
    }
    
    @Override
    public boolean isFakeBlockAt(int x, int y, int z) {
        FakeChunkBase chunk = getChunkAtRaw(x >> 4, z >> 4);
        if(chunk != null) {
            return chunk.isFakeBlock(x & 15, y, z & 15);
        }
        else {
            return false;
        }
    }
    
    @Override
    public FakeBlockBase getBlockAt(Location loc) {
        return getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    @Override
    public FakeBlockBase getBlockAt(Vector vec) {
        return getBlockAt(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
    }
    
    
    public FakeBlockBase getBlockAtRaw(int x, int y, int z) {
        FakeChunkBase chunk = getChunkAtRaw(x >> 4, z >> 4);
        if(chunk != null) {
            return chunk.getBlockRaw(x & 15, y, z & 15);
        }
        else {
            return null;
        }
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
        changes.add(change);
    }
    
    private long getKey(int x, int z) {
        long key = Integer.toUnsignedLong(x);
        return key << 32 | Integer.toUnsignedLong(z);
    }
    
    private Stream<FakeBlockBase> getUsedBlocksStream() {
        return chunks
                .values()
                .stream()
                .flatMap(fc -> fc.getUsedBlocks().stream());
    }

    @Override
    public Collection<FakeBlockBase> getUsedBlocks() {
        return getUsedBlocksStream()
                .collect(Collectors.toSet());
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count) {
        Set<Player> targets = fwm.getSubscribers(this);
        if(targets.isEmpty()) return;
        targets.forEach(p -> p.spawnParticle(particle, location, count));
    }
    
    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
        Set<Player> targets = fwm.getSubscribers(this);
        if(targets.isEmpty()) return;
        targets.forEach(p -> p.spawnParticle(particle, location, count, data));
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double xOffset, double yOffset, double zOffset) {
        Set<Player> targets = fwm.getSubscribers(this);
        if(targets.isEmpty()) return;
        targets.forEach(p -> p.spawnParticle(particle, location, count, xOffset, yOffset, zOffset));
    }
    
    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double xOffset, double yOffset, double zOffset, T data) {
        Set<Player> targets = fwm.getSubscribers(this);
        if(targets.isEmpty()) return;
        targets.forEach(p -> p.spawnParticle(particle, location, count, xOffset, yOffset, zOffset, data));
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double xOffset, double yOffset, double zOffset, double extra) {
        Set<Player> targets = fwm.getSubscribers(this);
        if(targets.isEmpty()) return;
        targets.forEach(p -> p.spawnParticle(particle, location, count, xOffset, yOffset, zOffset, extra));
    }
    
    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double xOffset, double yOffset, double zOffset, double extra, T data) {
        Set<Player> targets = fwm.getSubscribers(this);
        if(targets.isEmpty()) return;
        targets.forEach(p -> p.spawnParticle(particle, location, count, xOffset, yOffset, zOffset, extra, data));
    }
    
    @Override
    public <T> void playSound(Location location, Sound sound, float volume, float pitch) {
        Set<Player> targets = fwm.getSubscribers(this);
        if(targets.isEmpty()) return;
        targets.forEach(p -> p.playSound(location, sound, volume, pitch));
    }
    
    @Override
    public void playEffect(Location location, Effect effect, int data) {
        Set<Player> targets = fwm.getSubscribers(this);
        if(targets.isEmpty()) return;
        targets.forEach(p -> p.playEffect(location, effect, data));
    }

    @Override
    public <T> void playEffect(Location location, Effect effect, T data) {
        Set<Player> targets = fwm.getSubscribers(this);
        if(targets.isEmpty()) return;
        targets.forEach(p -> p.playEffect(location, effect, data));
    }

    @Override
    public void reset() {
        this.chunks.values().forEach(FakeChunkBase::reset);
    }

    @Override
    public boolean isFastEditing() {
        return fastEditing;
    }
    
    @Override
    public void setFastEditing(boolean fastEditing) {
        if(fastEditing && !this.fastEditing) {
            calculateBlockChanges();
        }
        this.fastEditing = fastEditing;
    }
    
    private void calculateBlockChanges() {
        getUsedBlocksStream()
                .filter(FakeBlockBase::hasChanged)
                .peek(FakeBlockBase::resetFastEditingMode)
                .map(fb -> new BlockChange(fb, ChangeType.PLUGIN, null, null))
                .forEach(fb -> notifyChange(fb));
    }
}
