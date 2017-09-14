/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.spleefleague.virtualworld.api.FakeWorld;
import com.spleefleague.virtualworld.protocol.MultiBlockChangeHandler;
import com.spleefleague.virtualworld.protocol.PacketBlockBreakAdapter;
import com.spleefleague.virtualworld.protocol.PacketBlockInteractAdapter;
import com.spleefleague.virtualworld.protocol.PacketBlockPlaceAdapter;
import com.spleefleague.virtualworld.protocol.PacketChunkLoadAdapter;
import com.spleefleague.virtualworld.protocol.PacketChunkUnloadAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 *
 * @author jonas
 */
public class VirtualWorld extends JavaPlugin implements Listener {

    private static VirtualWorld instance;
    private ProtocolManager manager;
    private FakeWorldManager fakeWorldManager;
    private FakeWorld test, test2;
    
    @Override
    public void onEnable() {
        instance = this;
        manager = ProtocolLibrary.getProtocolManager();
        MultiBlockChangeHandler mbchandler = MultiBlockChangeHandler.init();
        fakeWorldManager = FakeWorldManager.init(mbchandler);
        manager.addPacketListener(new PacketChunkLoadAdapter(fakeWorldManager, mbchandler));
        manager.addPacketListener(new PacketChunkUnloadAdapter(mbchandler));
        manager.addPacketListener(new PacketBlockBreakAdapter(fakeWorldManager));
        manager.addPacketListener(new PacketBlockInteractAdapter(fakeWorldManager));
        manager.addPacketListener(new PacketBlockPlaceAdapter(fakeWorldManager));
    }
    
    public ProtocolManager getProtocolManager() {
        return manager;
    }
    
    public FakeWorldManager getFakeWorldManager() {
        return fakeWorldManager;
    }
    
    public static VirtualWorld getInstance() {
        return instance;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FakeWorld fst = test, snd = test2;
        test = test2;
        test2 = fst;
        fakeWorldManager.addWorld(player, fst, 1);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            fakeWorldManager.addWorld(player, snd, 0);
        }, 10 * 20);
    }
    
//    @EventHandler
//    public void onFakeBreak(FakeBlockBreakEvent event) {
//        event.setCancelled(event.getPlayer().getName().equals("Geier"));
//    }
//    
//    @EventHandler
//    public void onFakePlace(FakeBlockPlaceEvent event) {
//        event.setCancelled(event.getPlayer().getName().equals("Geier"));
//    }
}
