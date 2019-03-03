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
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.IBlockData;
import org.bukkit.GameMode;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_13_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;

/**
 *
 * @author balsfull
 */
public class PacketBlockBreakAdapter extends PacketAdapter {

    private final FakeWorldManager fakeWorldManager;
    private final PacketOnGroundAdapter groundStateManager;
    
    public PacketBlockBreakAdapter(FakeWorldManager fwm, PacketOnGroundAdapter groundStateManager) {
        super(VirtualWorld.getInstance(), ListenerPriority.NORMAL, new PacketType[]{PacketType.Play.Client.BLOCK_DIG});
        fakeWorldManager = fwm;
        this.groundStateManager = groundStateManager;
    }
    
    @Override
    public void onPacketReceiving(PacketEvent event) {
        if(event.isCancelled()) return;
        WrapperPlayClientBlockDig ppcbd = new WrapperPlayClientBlockDig(event.getPacket());
        BlockPosition loc = ppcbd.getLocation();
        Player p = event.getPlayer();
        FakeBlockBase affected = (FakeBlockBase)fakeWorldManager.getBlockAt(p, p.getLocation().getWorld(), loc.getX(), loc.getY(), loc.getZ());
        if(affected == null) {
            return;
        }
        if(ppcbd.getStatus() == PlayerDigType.STOP_DESTROY_BLOCK || (ppcbd.getStatus() == PlayerDigType.START_DESTROY_BLOCK && isInstantlyDestroyed(p, affected.getType(), loc))) {
            Bukkit.getScheduler().runTask(VirtualWorld.getInstance(), () -> {
                FakeBlockBreakEvent breakEvent = new FakeBlockBreakEvent(affected, event.getPlayer());
                Bukkit.getPluginManager().callEvent(breakEvent);
                if(breakEvent.isCancelled()) {
                    Bukkit.getScheduler().runTaskLater(VirtualWorld.getInstance(), () -> {
                        p.sendBlockChange(new Location(p.getWorld(), loc.getX(), loc.getY(), loc.getZ()), affected.getBlockData());
                    }, 1);
                    return;
                }
                BlockData oldState = affected.getBlockdata().clone();
                affected._setType(Material.AIR);
                affected.registerChanged(ChangeType.BREAK, oldState, p);
            });
        }
        else {    
            event.setCancelled(true);
        }
    }
    
    public boolean isInstantlyDestroyed(Player player, Material type, BlockPosition bp) {
        if(type == Material.AIR) {
            return false;
        }
        if(player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }
        IBlockData ibd = ((CraftBlockData)type.createBlockData()).getState();
        EntityPlayer ep = ((CraftPlayer)player).getHandle();
        net.minecraft.server.v1_13_R2.BlockPosition nmsbp = new net.minecraft.server.v1_13_R2.BlockPosition(bp.getX(), bp.getY(), bp.getZ());
        float damage = ibd.getDamage(ep, ep.world, nmsbp);
        boolean spigotOnGround = player.isOnGround();
        boolean actualOnGround = groundStateManager.isOnGround(player);
        if(!spigotOnGround && actualOnGround) {
            damage *= 5;
        }
        return damage >= 1.0f;
    }
}
