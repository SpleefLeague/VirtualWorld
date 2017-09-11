/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.event;

import com.spleefleague.virtualworld.api.implementation.FakeBlockBase;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 *
 * @author jonas
 */
public class FakeBlockBreakEvent extends FakeBlockEvent{
    private static final HandlerList handlers = new HandlerList();

    public FakeBlockBreakEvent(FakeBlockBase block, Player player) {
        super(block, player);
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
