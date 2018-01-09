package com.spleefleague.virtualworld.protocol;

import com.comphenix.packetwrapper.WrapperPlayServerUnloadChunk;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.spleefleague.virtualworld.VirtualWorld;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

/**
 *
 * @author balsfull
 */
public class PacketChunkUnloadAdapter extends PacketAdapter {

    private final MultiBlockChangeHandler mbchandler;

    public PacketChunkUnloadAdapter(MultiBlockChangeHandler mbchandler) {
        super(VirtualWorld.getInstance(), ListenerPriority.NORMAL, new PacketType[]{PacketType.Play.Server.UNLOAD_CHUNK});
        this.mbchandler = mbchandler;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if(event.isCancelled()) return;
        WrapperPlayServerUnloadChunk wpsuc = new WrapperPlayServerUnloadChunk(event.getPacket());
        Bukkit.getScheduler().runTask(VirtualWorld.getInstance(), () -> {
            Chunk chunk = event.getPlayer().getWorld().getChunkAt(wpsuc.getChunkX(), wpsuc.getChunkZ());
            mbchandler.removeChunk(event.getPlayer(), chunk);
        });
    }
}
