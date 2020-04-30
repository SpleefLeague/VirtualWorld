/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.spleefleague.virtualworld.protocol.MultiBlockChangeHandler;
import com.spleefleague.virtualworld.protocol.NoClipHandler;
import com.spleefleague.virtualworld.protocol.PacketBlockBreakAdapter;
import com.spleefleague.virtualworld.protocol.PacketBlockChangeAdapter;
import com.spleefleague.virtualworld.protocol.PacketBlockInteractAdapter;
import com.spleefleague.virtualworld.protocol.PacketBlockPlaceAdapter;
import com.spleefleague.virtualworld.protocol.PacketChunkLoadAdapter;
import com.spleefleague.virtualworld.protocol.PacketChunkUnloadAdapter;
import com.spleefleague.virtualworld.protocol.PacketOnGroundAdapter;
import org.bukkit.Bukkit;
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
    private PacketChunkUnloadAdapter chunkUnloadManager;
    public PacketChunkLoadAdapter chunkLoadManager;
    private NoClipHandler noclipHandler;
    
    @Override
    public void onEnable() {
        instance = this;
        manager = ProtocolLibrary.getProtocolManager();
        MultiBlockChangeHandler mbchandler = MultiBlockChangeHandler.init();
        fakeWorldManager = FakeWorldManager.init(mbchandler);
        groundStateManager = new PacketOnGroundAdapter();
        chunkUnloadManager = new PacketChunkUnloadAdapter(mbchandler);
        chunkLoadManager = new PacketChunkLoadAdapter(fakeWorldManager, mbchandler);
        noclipHandler = new NoClipHandler();
        Bukkit.getPluginManager().registerEvents(groundStateManager, this);
        Bukkit.getPluginManager().registerEvents(noclipHandler, this);
        Bukkit.getPluginManager().registerEvents(chunkUnloadManager, this);
        Bukkit.getPluginManager().registerEvents(chunkLoadManager, this);
        manager.addPacketListener(groundStateManager);
        manager.addPacketListener(chunkLoadManager);
        manager.addPacketListener(chunkUnloadManager);
        manager.addPacketListener(new PacketBlockBreakAdapter(fakeWorldManager, groundStateManager));
        manager.addPacketListener(new PacketBlockInteractAdapter(fakeWorldManager));
        manager.addPacketListener(new PacketBlockChangeAdapter(fakeWorldManager));
        manager.addPacketListener(new PacketBlockPlaceAdapter(fakeWorldManager));
    }
    
    public ProtocolManager getProtocolManager() {
        return manager;
    }
    
    public FakeWorldManager getFakeWorldManager() {
        return fakeWorldManager;
    }

    public NoClipHandler getNoclipHandler() {
        return noclipHandler;
    }
    
    public boolean isOnGround(Player p) {
        return groundStateManager.isOnGround(p);
    }
    
    public void setChunkUnloadingDisabled(Player p, boolean disabled) {
        chunkUnloadManager.setChunkUnloadsDisabled(p, disabled);
    }
    
    public void setChunkLoadingDisabled(Player p, boolean disabled) {
        chunkUnloadManager.setChunkUnloadsDisabled(p, disabled);
    }
    
    public static VirtualWorld getInstance() {
        return instance;
    }
}
