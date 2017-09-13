package com.spleefleague.virtualworld.api.implementation;

import com.spleefleague.virtualworld.api.implementation.BlockChange.ChangeType;
import com.spleefleague.virtualworld.api.FakeBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 *
 * @author balsfull
 */
public class FakeBlockBase extends FakeBlockPrototype {
    
    protected FakeBlockBase(FakeChunkBase chunk, int x, int y, int z) {
        super(chunk, x, y, z);
    }

    @Override
    public void setType(Material type, boolean force) {
        if(!force && getHandle().getType() != Material.AIR) return;
        super.setType(type, force);
        registerChanged(ChangeType.PLUGIN);
    }
    
    public void _setType(Material type) {
        super.setType(type, true);
    }
    
    @Override
    public void setData(byte data) {
        setData(data, false);
    }
    
    @Override
    public void setData(byte data, boolean force) {
        if(!force && getHandle().getType() != Material.AIR) return;
        super.setData(data, force);
        registerChanged(ChangeType.PLUGIN);
    }
    
    public void _setData(byte data) {
        super.setData(data, true);
    }
    
    public void registerChanged(ChangeType changeType) {
        this.getChunk().notifyChange(new BlockChange(this, changeType));
    }
}
