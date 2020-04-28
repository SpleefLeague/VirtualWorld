/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.spleefleague.virtualworld.protocol.MultiBlockChangeHandler;
import com.spleefleague.virtualworld.protocol.PacketBlockBreakAdapter;
import com.spleefleague.virtualworld.protocol.PacketBlockInteractAdapter;
import com.spleefleague.virtualworld.protocol.PacketBlockPlaceAdapter;
import com.spleefleague.virtualworld.protocol.PacketChunkLoadAdapter;
import com.spleefleague.virtualworld.protocol.PacketChunkUnloadAdapter;
import com.spleefleague.virtualworld.protocol.PacketOnGroundAdapter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author jonas
 */
public class VirtualWorld extends JavaPlugin {

    private static VirtualWorld instance;
    private ProtocolManager manager;
    private FakeWorldManager fakeWorldManager;
    private PacketOnGroundAdapter groundStateManager;
    private World defaultWorld;
    
    @Override
    public void onEnable() {
        instance = this;
        manager = ProtocolLibrary.getProtocolManager();
        MultiBlockChangeHandler mbchandler = MultiBlockChangeHandler.init();
        fakeWorldManager = FakeWorldManager.init(mbchandler);
        groundStateManager = new PacketOnGroundAdapter();
        Bukkit.getPluginManager().registerEvents(groundStateManager, this);
        manager.addPacketListener(groundStateManager);
        manager.addPacketListener(new PacketChunkLoadAdapter(fakeWorldManager, mbchandler));
        manager.addPacketListener(new PacketChunkUnloadAdapter(mbchandler));
        manager.addPacketListener(new PacketBlockBreakAdapter(fakeWorldManager, groundStateManager));
        manager.addPacketListener(new PacketBlockInteractAdapter(fakeWorldManager));
        manager.addPacketListener(new PacketBlockPlaceAdapter(fakeWorldManager));
    }
    
    public ProtocolManager getProtocolManager() {
        return manager;
    }
    
    public FakeWorldManager getFakeWorldManager() {
        return fakeWorldManager;
    }
    
    public boolean isOnGround(Player p) {
        return groundStateManager.isOnGround(p);
    }
    
    public void setDefaultWorld(World world) {
        defaultWorld = world;
    }
    
    public World getDefaultWorld() {
        return defaultWorld;
    }
    
    public static VirtualWorld getInstance() {
        return instance;
    }
}
