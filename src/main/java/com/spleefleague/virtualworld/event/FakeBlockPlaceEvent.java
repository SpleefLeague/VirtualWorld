package com.spleefleague.virtualworld.event;

import com.spleefleague.virtualworld.api.FakeBlock;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockMultiPlaceEvent;

/**
 *
 * @author balsfull
 */
public class FakeBlockPlaceEvent extends FakeBlockEvent{

    private final BlockData blockData;
    
    public FakeBlockPlaceEvent(Player player, FakeBlock replaced, Material targetType) {
        this(player, replaced, targetType.createBlockData());
    }
    
    public FakeBlockPlaceEvent(Player player, FakeBlock replaced, BlockData blockData) {
        super(replaced, player);
        this.blockData = blockData;
    }

    public BlockData getBlockData() {
        return blockData;
    }
}
