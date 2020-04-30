/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld;

import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.PacketPlayInFlying;
import net.minecraft.server.v1_15_R1.PlayerConnection;

/**
 *
 * @author jonas
 */
public class NoClipPlayerConnection extends PlayerConnection {
    
    private boolean noclip = false;
    
    public NoClipPlayerConnection(MinecraftServer server, PlayerConnection con) {
        super(server, con.networkManager, con.player);
    }
    
    @Override
    public void a(PacketPlayInFlying ppif) {
        if(noclip) {
            this.player.noclip = true;
            super.a(ppif);
            this.player.noclip = false;
        }
        else {
            super.a(ppif);
        }
    }

    public boolean isNoClip() {
        return noclip;
    }

    public void setNoClip(boolean noclip) {
        this.noclip = noclip;
    }
}
