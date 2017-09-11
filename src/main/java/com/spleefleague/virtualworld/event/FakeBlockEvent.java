/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.event;

import com.spleefleague.virtualworld.api.implementation.FakeBlockBase;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author jonas
 */
public abstract class FakeBlockEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final FakeBlockBase block;
    private boolean cancelled = false;

    public FakeBlockEvent(FakeBlockBase block, Player player) {
        this.player = player;
        this.block = block;
    }

    public Player getPlayer() {
        return player;
    }

    public FakeBlockBase getBlock() {
        return block;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}