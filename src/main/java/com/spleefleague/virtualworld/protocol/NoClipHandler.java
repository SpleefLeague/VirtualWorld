/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.protocol;

import com.spleefleague.virtualworld.NoClipPlayerConnection;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author jonas
 */
public class NoClipHandler implements Listener {
    
    private Field mcServerField;
    
    public NoClipHandler() {
        try {
            mcServerField = PlayerConnection.class.getDeclaredField("minecraftServer");
            mcServerField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(NoClipHandler.class.getName()).log(Level.SEVERE, "NoClip handler shutting down", ex);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws IllegalAccessException {
        if(mcServerField == null) return;
        PlayerConnection con = ((CraftPlayer)event.getPlayer()).getHandle().playerConnection;
        MinecraftServer mcs = (MinecraftServer)mcServerField.get(con);
        ((CraftPlayer)event.getPlayer()).getHandle().playerConnection = new NoClipPlayerConnection(mcs, con);
    }
    
    public void setNoClip(Player p, boolean noclip) {
        getNCPConnection(p).setNoClip(noclip);
    }
    
    public boolean getNoClip(Player p) {
        return getNCPConnection(p).isNoClip();
    }
    
    private NoClipPlayerConnection getNCPConnection(Player player) {
        return (NoClipPlayerConnection)((CraftPlayer)player).getHandle().playerConnection;
    }
}
