package com.spleefleague.virtualworld.api.implementation;

import com.spleefleague.virtualworld.api.implementation.BlockChange.ChangeType;
import com.spleefleague.virtualworld.api.FakeBlock;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

/**
 *
 * @author balsfull
 */
public class FakeBlockBase implements FakeBlock {
    
    private final FakeChunkBase chunk;
    private final int x, y, z;
    private BlockData blockData;
    private BlockData originalBlockData;

    public FakeBlockBase(FakeChunkBase chunk, int x, int y, int z) {
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
        if(chunk != null) {
            Block handle = getHandle();
            blockData = handle.getBlockData();
        }
        else {
            blockData = null;
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

    @Override
    public Material getType() {
        return blockData.getMaterial();
    }

    @Override
    public BlockData getBlockData() {
        return blockData;
    }
    
    @Override
    public void setType(Material type) {
        setType(type, false);
    }

    @Override
    public void setBlockData(BlockData data) {
        setBlockData(data, false);
    }
    
    @Override
    public void setBlockData(BlockData blockData, boolean force) {
        if(!force && getHandle().getType() != Material.AIR) return;
        if(blockData.equals(this.blockData)) return;
        BlockData oldState = blockData.clone();
        _setBlockData(blockData);
        if(!chunk.getWorld().isFastEditing()) {
            registerChanged(ChangeType.PLUGIN, oldState, null);
        }
    }
    
    public void _setType(Material type) {
        _setBlockData(type.createBlockData());
    }
    
    public void _setBlockData(BlockData blockData) {
        if(chunk.getWorld().isFastEditing() && originalBlockData == null) {
            this.originalBlockData = this.blockData;
        }
        this.blockData = blockData;
    }
    
    public boolean hasNaturalState() {
        Block handle = getHandle();
        return handle.getBlockData().equals(blockData);
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
        return Objects.equals(this.chunk, other.chunk);
    }
    
    protected boolean hasChanged() {
        if(originalBlockData == null) return false;
        return this.blockData.getMaterial() != this.originalBlockData.getMaterial();
    }
    
    protected BlockData getOriginalBlockData() {
        return originalBlockData;
    }
    
    protected void resetFastEditingMode() {
        this.originalBlockData = null;
    }

    @Override
    public void reset() {
        this.setBlockData(getHandle().getBlockData(), true);
//        this.setType(getHandle().getType(), true);
//        this.registerChanged(ChangeType.PLUGIN, blockData, null);
//        this.chunk.removeBlock(this);
    }
    
//    public void _reset() {
//        this.setBlockData(getHandle().getBlockData(), true);
//    }

    @Override
    public FakeBlock getRelative(BlockFace face) {
        return chunk.getWorld().getBlockAt(x + face.getModX(), y + face.getModY(), z + face.getModZ());
    }
}
