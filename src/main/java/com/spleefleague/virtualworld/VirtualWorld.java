/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.spleefleague.virtualworld.api.FakeWorld;
import com.spleefleague.virtualworld.event.FakeBlockBreakEvent;
import com.spleefleague.virtualworld.event.FakeBlockPlaceEvent;
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
        Bukkit.getPluginManager().registerEvents(this, this);
        Vector lo = new Vector (100, 100, 100);
        Vector hi = new Vector (130, 130, 130);
        Area area = new Area(hi, lo);
        test = VirtualWorld.getInstance().getFakeWorldManager().createWorld(Bukkit.getWorlds().get(0), area);
        for (int x = 100; x < 120; x++) {
            for (int y = 100; y < 120; y++) {
                for (int z = 100; z < 120; z++) {
                    test.getBlockAt(x, y, z).setType(Material.SNOW_BLOCK);
                }
            }
        }
        lo = new Vector (110, 110, 110);
        hi = new Vector (140, 140, 140);
        area = new Area(hi, lo);
        test2 = VirtualWorld.getInstance().getFakeWorldManager().createWorld(Bukkit.getWorlds().get(0), area);
        for (int x = 110; x < 130; x++) {
            for (int y = 110; y < 130; y++) {
                for (int z = 110; z < 130; z++) {
                    test2.getBlockAt(x, y, z).setType(Material.GLASS);
                }
            }
        }
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
        fakeWorldManager.addWorld(player, test, 0);
        fakeWorldManager.addWorld(player, test2, 1);
        FakeWorld tmp = test;
        test = test2;
        test2 = tmp;
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
