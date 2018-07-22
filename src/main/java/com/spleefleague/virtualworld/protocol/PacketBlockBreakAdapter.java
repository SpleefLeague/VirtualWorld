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
import net.minecraft.server.v1_13_R1.Block;
import net.minecraft.server.v1_13_R1.EntityHuman;
import net.minecraft.server.v1_13_R1.IBlockData;
import org.bukkit.GameMode;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;

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
        if(ppcbd.getStatus() == PlayerDigType.STOP_DESTROY_BLOCK || (ppcbd.getStatus() == PlayerDigType.START_DESTROY_BLOCK && isInstantlyDestroyed(p, affected.getType()))) {
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
    
    public boolean isInstantlyDestroyed(Player player, Material type) {
        if(type == Material.AIR) {
            return false;
        }
        if(player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }
        Block block = Block.getById(type.getId());
        EntityHuman entityhuman = ((CraftPlayer)player).getHandle();
        boolean hasBlock = entityhuman.hasBlock(block.getBlockData());
        float strength = block.a((IBlockData)null, null, null);//Block strength
        float destructionValue;
        if(!hasBlock) {
            destructionValue = entityhuman.b(block.getBlockData()) / strength / 100.0F;
        }
        else if(strength <= 0.0F) {
            return false;
        }
        else {
            destructionValue = entityhuman.b(block.getBlockData()) / strength / 30.0F;
        }
        if(destructionValue < 0.2F || !groundStateManager.isOnGround(player)) {
            return false;
        }
        return true;
    }
}
