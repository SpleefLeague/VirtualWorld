package com.spleefleague.virtualworld.protocol;

import com.comphenix.protocol.PacketType;
import static com.comphenix.protocol.PacketType.Play.Client.FLYING;
import static com.comphenix.protocol.PacketType.Play.Client.LOOK;
import static com.comphenix.protocol.PacketType.Play.Client.POSITION;
import static com.comphenix.protocol.PacketType.Play.Client.POSITION_LOOK;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.spleefleague.virtualworld.VirtualWorld;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author balsfull
 */
public class PacketOnGroundAdapter extends PacketAdapter implements Listener {

    private final Map<UUID, Boolean> onGround = new HashMap<>();
    
    public PacketOnGroundAdapter() {
        super(VirtualWorld.getInstance(), ListenerPriority.NORMAL, new PacketType[]{FLYING, POSITION, POSITION_LOOK, LOOK});
    }
    
    public boolean isOnGround(Player player) {
        return player.isOnGround() || onGround.get(player.getUniqueId());
    }
    
    @Override
    public void onPacketReceiving(PacketEvent event) {
        if(event.isCancelled()) return;
        PacketType type = event.getPacketType();
        boolean onGround = false;
        if(type == FLYING) {
            onGround = event.getPlayer().isOnGround();
        }
        else if(type == POSITION) {
            onGround = event.getPlayer().isOnGround();
        }
        else if(type == POSITION_LOOK) {
            onGround = event.getPlayer().isOnGround();
        }
        else if(type == LOOK) {
            onGround = event.getPlayer().isOnGround();
        }
        this.onGround.put(event.getPlayer().getUniqueId(), onGround);
    }
    
    @Override
    public void onPacketSending(PacketEvent event) {
        
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        this.onGround.put(event.getPlayer().getUniqueId(), Boolean.FALSE);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.onGround.remove(event.getPlayer().getUniqueId());
    }
}
