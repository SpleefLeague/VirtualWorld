package com.spleefleague.virtualworld.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
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
        PacketContainer packetContainer = event.getPacket();
        ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(packetContainer.getIntegers().read(0), packetContainer.getIntegers().read(1));
        Bukkit.getScheduler().runTask(VirtualWorld.getInstance(), () -> {
            Chunk chunk = event.getPlayer().getWorld().getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
            mbchandler.removeChunk(event.getPlayer(), chunk);
        });
    }
}
