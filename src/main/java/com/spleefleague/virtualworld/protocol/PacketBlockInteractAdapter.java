package com.spleefleague.virtualworld.protocol;

import com.comphenix.packetwrapper.WrapperPlayClientUseItem;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.spleefleague.virtualworld.FakeWorldManager;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.api.implementation.FakeBlockBase;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 *
 * @author balsfull
 */
public class PacketBlockInteractAdapter extends PacketAdapter {

    private final FakeWorldManager fwmanager;

    public PacketBlockInteractAdapter(FakeWorldManager fwmanager) {
        super(VirtualWorld.getInstance(), ListenerPriority.NORMAL, new PacketType[]{PacketType.Play.Client.USE_ITEM});
        this.fwmanager = fwmanager;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        WrapperPlayClientUseItem wrapper = new WrapperPlayClientUseItem(event.getPacket());
        if (wrapper.getLocation().getY() < 0) {
            return;
        }
        Location loc = wrapper.getLocation().toVector().toLocation(event.getPlayer().getWorld());
        FakeBlock block = fwmanager.getBlockAt(event.getPlayer(), loc);
        if(block != null) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {

    }

    private boolean vectorEqual(Vector loc1, Vector loc2) {
        if ((loc1.getX() + 0.5) / 1.0 == (loc2.getX() + 0.5) / 1.0 && (loc1.getZ() + 0.5) / 1.0 == (loc2.getZ() + 0.5) / 1.0 && loc1.getY() / 1.0 == loc2.getY() / 1.0) {
            return true;
        }
        return false;
    }
}