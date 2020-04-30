package com.spleefleague.virtualworld.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.spleefleague.virtualworld.FakeWorldManager;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.FakeChunk;
import java.util.ArrayList;
import java.util.List;
import static net.minecraft.server.v1_15_R1.BlockProperties.z;
import static net.minecraft.server.v1_15_R1.Foods.y;
import static net.minecraft.server.v1_15_R1.WorldGenSurface.x;
import org.bukkit.entity.Player;

/**
 *
 * @author balsfull
 */
public class PacketBlockChangeAdapter extends PacketAdapter {

    private final FakeWorldManager fakeWorldManager;
    
    public PacketBlockChangeAdapter(FakeWorldManager fwm) {
        super(VirtualWorld.getInstance(), ListenerPriority.HIGH, new PacketType[]{PacketType.Play.Server.BLOCK_CHANGE, PacketType.Play.Server.MULTI_BLOCK_CHANGE});
        fakeWorldManager = fwm;
    }
    
    @Override
    public void onPacketSending(PacketEvent event) {
        if(event.isCancelled()) return;
        Player p = event.getPlayer();
        if(event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
            BlockPosition loc = event.getPacket().getBlockPositionModifier().read(0);
            boolean fakeblock = fakeWorldManager.isFakeBlock(p, p.getWorld(), loc.getX(), loc.getY(), loc.getZ());
            if(fakeblock) {
                event.setCancelled(true);
            }
        }
        else {
            ChunkCoordIntPair chunkCoords = event.getPacket().getChunkCoordIntPairs().read(0);
            List<FakeChunk> potentialChunks = fakeWorldManager.getChunkList(p, p.getWorld(), chunkCoords.getChunkX(), chunkCoords.getChunkZ());
            if(potentialChunks.isEmpty()) return;
            MultiBlockChangeInfo[] mbciArr = event.getPacket().getMultiBlockChangeInfoArrays().read(0);
            List<MultiBlockChangeInfo> allowed = new ArrayList<>(mbciArr.length);
            for(MultiBlockChangeInfo mbci : mbciArr) {
                if(isChangePermitted(mbci, potentialChunks)) {
                    allowed.add(mbci);
                }
            }
            if(allowed.size() == mbciArr.length) return;
            mbciArr = allowed.toArray(new MultiBlockChangeInfo[allowed.size()]);
            event.getPacket().getMultiBlockChangeInfoArrays().write(0, mbciArr);
        }
    }
    
    private boolean isChangePermitted(MultiBlockChangeInfo mbci, List<FakeChunk> fakeChunkList) {
        int x = mbci.getX();
        int y = mbci.getY();
        int z = mbci.getZ();
        return fakeChunkList.stream().noneMatch(fc -> fc.isFakeBlock(x, y, z));
    }
}
