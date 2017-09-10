package com.spleefleague.virtualworld;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author balsfull
 */
public class FakeWorldManager implements Listener {
    
    private Map<Player, BlockStateManager> stateManager;
    
    public FakeWorldManager() {
        //Register listener
    }
    
    public void notifyStateChange(FakeBlock block) {
        stateManager.values().forEach(b -> b.notifyStateChange(block));
    }
    
    public void notifyStateChange(FakeBlock block, Player player) {
        stateManager.get(player).notifyStateChange(block);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        stateManager.put(event.getPlayer(), new BlockStateManager());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        stateManager.remove(event.getPlayer());
    }
}
