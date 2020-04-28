package com.spleefleague.virtualworld.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.spleefleague.virtualworld.FakeWorldManager;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.protocol.chunk.ChunkPacketInjector;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;

/**
 *
 * @author balsfull
 */
public class PacketChunkLoadAdapter extends PacketAdapter {

    private final FakeWorldManager fwmanager;
    private final MultiBlockChangeHandler mbchandler;
    
    public PacketChunkLoadAdapter(FakeWorldManager fwmanager, MultiBlockChangeHandler mbchandler) {
        super(VirtualWorld.getInstance(), ListenerPriority.NORMAL, new PacketType[]{PacketType.Play.Server.MAP_CHUNK});
        this.fwmanager = fwmanager;
        this.mbchandler = mbchandler;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if(event.isCancelled()
                || !event.getPlayer().getWorld().getName().equalsIgnoreCase(VirtualWorld.getInstance().getDefaultWorld().getName())) return;
        //if (!event.getPlayer().equals(VirtualWorld.getInstance().getDefaultWorld())) return;
        PacketContainer packetContainer = event.getPacket();
        ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(packetContainer.getIntegers().read(0), packetContainer.getIntegers().read(1));
        Bukkit.getScheduler().runTask(VirtualWorld.getInstance(), () -> {
            Chunk chunk = event.getPlayer().getWorld().getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
            mbchandler.addChunk(event.getPlayer(), chunk);
        });
        Collection<FakeBlock> blocks = fwmanager.getBlocksInChunk(event.getPlayer(), chunkCoord.getChunkX(), chunkCoord.getChunkZ());
        if (!blocks.isEmpty()) {
            //ChunkPacketInjector.setBlocksPacketMapChunk(event.getPlayer().getWorld(), event.getPacket(), blocks);
            Bukkit.getScheduler().runTaskLater(VirtualWorld.getInstance(), () -> {
                PacketContainer packetMultiBlock = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
                packetMultiBlock.getChunkCoordIntPairs().write(0, chunkCoord);
                MultiBlockChangeInfo[] mbci = new MultiBlockChangeInfo[blocks.size()];
                int i = 0;
                for (FakeBlock fb : blocks) {
                    WrappedBlockData wbd = WrappedBlockData.createData(fb.getType());
                    mbci[i] = new MultiBlockChangeInfo(fb.getLocation(), wbd);
                    i++;
                }
                packetMultiBlock.getMultiBlockChangeInfoArrays().write(0, mbci);
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packetMultiBlock);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(PacketChunkLoadAdapter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }, 1L);
        }
    }
}
