/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author jonas
 */
public class VirtualWorld extends JavaPlugin {

    private static VirtualWorld instance;
    private ProtocolManager manager;
    
    @Override
    public void onEnable() {
        instance = this;
        manager = ProtocolLibrary.getProtocolManager();
    }
    
    public ProtocolManager getProtocolManager() {
        return manager;
    }
    
    public static VirtualWorld getInstance() {
        return instance;
    }
}
