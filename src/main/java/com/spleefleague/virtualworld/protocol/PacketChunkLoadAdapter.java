package com.spleefleague.virtualworld.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.spleefleague.virtualworld.FakeWorldManager;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.protocol.chunk.ChunkPacketInjector;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author balsfull
 */
public class PacketChunkLoadAdapter extends PacketAdapter implements Listener {

    private final FakeWorldManager fwmanager;
    private final MultiBlockChangeHandler mbchandler;
    private final Set<UUID> loadingDisabled = new HashSet<>();
    
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
        if(event.isCancelled()) return;
        if(loadingDisabled.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        int chunkX = event.getPacket().getIntegers().read(0);
        int chunkZ = event.getPacket().getIntegers().read(1);
        Bukkit.getScheduler().runTask(VirtualWorld.getInstance(), () -> {
            Chunk chunk = event.getPlayer().getWorld().getChunkAt(chunkX, chunkZ);
            mbchandler.addChunk(event.getPlayer(), chunk);
        });
        Collection<FakeBlock> blocks = fwmanager.getBlocksInChunk(event.getPlayer(), chunkX, chunkZ);
        if (!blocks.isEmpty()) {
            ChunkPacketInjector.setBlocksPacketMapChunk(event.getPlayer().getWorld(), event.getPacket(), blocks);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.loadingDisabled.remove(event.getPlayer().getUniqueId());
    }
}
