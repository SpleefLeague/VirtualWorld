package com.spleefleague.virtualworld.protocol;

import com.comphenix.packetwrapper.WrapperPlayClientBlockDig;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import com.spleefleague.virtualworld.api.implementation.BlockChange.ChangeType;
import com.spleefleague.virtualworld.api.implementation.FakeBlockBase;
import com.spleefleague.virtualworld.FakeWorldManager;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.event.FakeBlockBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author balsfull
 */
public class PacketBlockBreakAdapter extends PacketAdapter {

    private final FakeWorldManager fakeWorldManager;
    
    public PacketBlockBreakAdapter(FakeWorldManager fwm) {
        super(VirtualWorld.getInstance(), ListenerPriority.NORMAL, new PacketType[]{PacketType.Play.Client.BLOCK_DIG});
        fakeWorldManager = fwm;
    }
    
    @Override
    public void onPacketReceiving(PacketEvent event) {
        WrapperPlayClientBlockDig ppcbd = new WrapperPlayClientBlockDig(event.getPacket());
        BlockPosition loc = ppcbd.getLocation();
        Player p = event.getPlayer();
        FakeBlockBase affected = fakeWorldManager.getBlockAt(p, p.getLocation().getWorld(), loc.getX(), loc.getY(), loc.getZ());
        if(affected == null) {
            return;
        }
        System.out.println(ppcbd.getStatus());
        if(ppcbd.getStatus() == PlayerDigType.STOP_DESTROY_BLOCK) {
            runSync(() -> {
                FakeBlockBreakEvent breakEvent = new FakeBlockBreakEvent(affected, event.getPlayer());
                Bukkit.getPluginManager().callEvent(breakEvent);
                if(breakEvent.isCancelled()) {
                    runSync(() -> {
                        p.sendBlockChange(new Location(p.getWorld(), loc.getX(), loc.getY(), loc.getZ()), affected.getType(), affected.getData());
                    }, 1);
                    return;
                }
                affected._setType(Material.AIR);
                affected._setData((byte)0);
                affected.registerChanged(ChangeType.BREAK);
            });
        }
    }
    
    private void runSync(Runnable run) {
        Bukkit.getScheduler().runTask(VirtualWorld.getInstance(), run);
    }
    
    private void runSync(Runnable run, int delay) {
        Bukkit.getScheduler().runTaskLater(VirtualWorld.getInstance(), run, delay);
    }
}
