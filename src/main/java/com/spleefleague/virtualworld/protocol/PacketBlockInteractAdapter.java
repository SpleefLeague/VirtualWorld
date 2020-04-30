package com.spleefleague.virtualworld.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.spleefleague.virtualworld.FakeWorldManager;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.FakeBlock;
import net.minecraft.server.v1_15_R1.MovingObjectPositionBlock;
import net.minecraft.server.v1_15_R1.PacketPlayInUseItem;
import org.bukkit.Location;

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
        if(event.isCancelled()) return;
        PacketPlayInUseItem ppiui = (PacketPlayInUseItem)event.getPacket().getHandle();
        net.minecraft.server.v1_15_R1.BlockPosition nmsPosition = ppiui.c().getBlockPosition();
        if (nmsPosition.getY() < 0) {
            return;
        }
        Location loc = new Location(event.getPlayer().getWorld(), nmsPosition.getX(), nmsPosition.getY(), nmsPosition.getZ());
        FakeBlock block = fwmanager.getBlockAt(event.getPlayer(), loc);
        if(block != null) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {

    }
}