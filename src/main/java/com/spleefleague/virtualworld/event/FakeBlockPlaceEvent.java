package com.spleefleague.virtualworld.event;

import com.spleefleague.virtualworld.api.implementation.FakeBlockBase;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author balsfull
 */
public class FakeBlockPlaceEvent extends FakeBlockEvent{

    private final Material type;
    private final byte data;
    
    public FakeBlockPlaceEvent(Player player, FakeBlockBase replaced, Material targetType) {
        this(player, replaced, targetType, (byte)0);
    }
    
    public FakeBlockPlaceEvent(Player player, FakeBlockBase replaced, Material targetType, byte targetData) {
        super(replaced, player);
        this.type = targetType;
        this.data = targetData;
    }
    
    public Material getTargetType() {
        return type;
    }
    
    public byte getTargetData() {
        return data;
    }
}
