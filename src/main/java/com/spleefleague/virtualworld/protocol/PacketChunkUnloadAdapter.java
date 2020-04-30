package com.spleefleague.virtualworld.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.spleefleague.virtualworld.VirtualWorld;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author balsfull
 */
public class PacketChunkUnloadAdapter extends PacketAdapter implements Listener {

    private final MultiBlockChangeHandler mbchandler;
    private final Set<UUID> unloadingDisabled = new HashSet<>();

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
        if(unloadingDisabled.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        int chunkX = event.getPacket().getIntegers().read(0);
        int chunkZ = event.getPacket().getIntegers().read(1);
        
        Bukkit.getScheduler().runTask(VirtualWorld.getInstance(), () -> {
            Chunk chunk = event.getPlayer().getWorld().getChunkAt(chunkX, chunkZ);
            mbchandler.removeChunk(event.getPlayer(), chunk);
        });
    }
    
    public void setChunkUnloadsDisabled(Player player, boolean enabled) {
        if(enabled) {
            this.unloadingDisabled.add(player.getUniqueId());
        }
        else {
            this.unloadingDisabled.remove(player.getUniqueId());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.unloadingDisabled.remove(event.getPlayer().getUniqueId());
    }
}
