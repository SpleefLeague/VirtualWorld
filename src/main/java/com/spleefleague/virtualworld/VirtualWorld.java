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
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author jonas
 */
public class VirtualWorld extends JavaPlugin {

    private static VirtualWorld instance;
    private ProtocolManager manager;
    private FakeWorldManager fakeWorldManager;
    
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
        //manager.addPacketListener(new PacketBlockPlaceAdapter(fakeWorldManager));
    }
    
    public ProtocolManager getProtocolManager() {
        return manager;
    }
    
    public static VirtualWorld getInstance() {
        return instance;
    }
}
