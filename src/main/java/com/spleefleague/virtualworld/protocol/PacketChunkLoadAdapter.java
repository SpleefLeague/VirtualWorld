package com.spleefleague.virtualworld.protocol;

import com.comphenix.packetwrapper.WrapperPlayServerMapChunk;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.spleefleague.virtualworld.FakeWorldManager;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.implementation.FakeBlockBase;
import com.spleefleague.virtualworld.protocol.chunk.ChunkPacketInjector;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

/**
 *
 * @author balsfull
 */
public class PacketChunkLoadAdapter extends PacketAdapter {

    private final FakeWorldManager fwmanager;
    private final MultiBlockChangeHandler mbchandler;
    
    public PacketChunkLoadAdapter(FakeWorldManager fwmanager, MultiBlockChangeHandler mbchandler) {
        super(VirtualWorld.getInstance(), ListenerPriority.NORMAL, new PacketType[]{PacketType.Play.Server.MAP_CHUNK});
        this.fwmanager = fwmanager;
        this.mbchandler = mbchandler;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerMapChunk wpsmc = new WrapperPlayServerMapChunk(event.getPacket());
        Bukkit.getScheduler().runTask(VirtualWorld.getInstance(), () -> {
            Chunk chunk = event.getPlayer().getWorld().getChunkAt(wpsmc.getChunkX(), wpsmc.getChunkZ());
            mbchandler.addChunk(event.getPlayer(), chunk);
        });
        Collection<FakeBlockBase> blocks = fwmanager.getBlocksInChunk(event.getPlayer(), wpsmc.getChunkX(), wpsmc.getChunkZ());
        if (!blocks.isEmpty()) {
            ChunkPacketInjector.setBlocksPacketMapChunk(event.getPlayer().getWorld(), event.getPacket(), blocks);
        }
    }
}
