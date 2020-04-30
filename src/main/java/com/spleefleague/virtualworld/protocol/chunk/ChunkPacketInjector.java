package com.spleefleague.virtualworld.protocol.chunk;


import com.comphenix.protocol.events.PacketContainer;
import com.spleefleague.virtualworld.api.FakeBlock;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.v1_15_R1.PacketPlayOutMapChunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.data.BlockData;

/**
 *
 * @author Jonas
 */
public class ChunkPacketInjector {
    
    public static void setBlocksPacketMapChunk(World world, PacketContainer packetContainer, Collection<FakeBlock> chunkBlocks) {
        if (packetContainer.getHandle() instanceof PacketPlayOutMapChunk) {
            int x = packetContainer.getIntegers().read(0);
            int z = packetContainer.getIntegers().read(1);
            Map<Integer, Collection<FakeBlock>> verified = toSectionMap(chunkBlocks);
            if (verified.size() > 0) {
                try {
                    byte[] bytes = packetContainer.getByteArrays().read(0); //Field d in 1.13, and f in 1.15
                    int bitmask = packetContainer.getIntegers().read(2); //Field c
                    int originalMask = bitmask;
                    for (int i : verified.keySet()) {
                        bitmask |= 1 << i;
                    }
                    ChunkData chunkData = splitToChunkSections(bitmask, originalMask, bytes, world.getEnvironment() == Environment.NORMAL);
                    insertFakeBlocks(chunkData.getSections(), verified);

                    byte[] data = toByteArray(chunkData);
                    packetContainer.getByteArrays().write(0, data);
                    packetContainer.getIntegers().write(2, bitmask);
                } catch (IOException | NullPointerException e) {
                    Logger.getLogger(ChunkPacketInjector.class.getName()).log(Level.SEVERE, "Debug info: ({0}|{1})", new Object[]{x, z});
                    Logger.getLogger(ChunkPacketInjector.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private static byte[] toByteArray(ChunkData data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (ChunkSection section : data.getSections()) {
            if (section != null) {
                writeChunkSectionData(baos, section);
            }
        }
        baos.write(data.getAdditionalData());
        return baos.toByteArray();
    }

    private static void writeChunkSectionData(ByteArrayOutputStream baos, ChunkSection section) throws IOException {
        BlockData[] used = section.getContainedBlocks();
        BlockPalette palette;
        if (used == null) {
            palette = BlockPalette.GLOBAL;
        } else {
            palette = BlockPalette.createPalette(used);
        }
        short nonAirCount = section.getNonAirCount();
        byte bpb = (byte) palette.getBitsPerBlock();
        int paletteLength = palette.getLength();
        int[] paletteInfo;
        if (paletteLength == 0) {
            paletteInfo = new int[0];
        } else {
            paletteInfo = palette.getPaletteData();
        }
        baos.write((nonAirCount >> 8) & 0xFF);
        baos.write(nonAirCount & 0xFF);
        baos.write(bpb);
        if(palette.includePaletteLength()) {
            ByteBufferReader.writeVarIntToByteArrayOutputStream(paletteLength, baos);
        }
        for (int p : paletteInfo) {
            ByteBufferReader.writeVarIntToByteArrayOutputStream(p, baos);
        }
        byte[] blockdata = palette.encode(section.getBlockData());
        ByteBufferReader.writeVarIntToByteArrayOutputStream(blockdata.length / 8/*it's represented as a long array*/, baos);
        baos.write(blockdata);
    }

    private static void insertFakeBlocks(ChunkSection[] sections, Map<Integer, Collection<FakeBlock>> blocks) {
        for (Entry<Integer, Collection<FakeBlock>> e : blocks.entrySet()) {
            int id = e.getKey();
            ChunkSection section = sections[id];
            for (FakeBlock block : e.getValue()) {
                int relX = block.getX() & 15; //Actual positive modulo, in java % means remainder. Only works as replacement for mod of powers of two
                int relZ = block.getZ() & 15; //Can be replaced with ((block.getZ() % 16) + 16) % 16
                boolean previouslyAir = section.getBlockRelative(relX, block.getY() % 16, relZ).getMaterial() == Material.AIR;
                section.setBlockRelative(block.getBlockData(), relX, block.getY() % 16, relZ);
                if(previouslyAir) {
                    if(block.getBlockData().getMaterial() != Material.AIR) {
                        section.setNonAirCount((short) (section.getNonAirCount() + 1));
                    }
                }
                else {
                    if(block.getBlockData().getMaterial() == Material.AIR) {
                        section.setNonAirCount((short) (section.getNonAirCount() - 1));
                    }
                }
            }
        }
    }

    private static ChunkData splitToChunkSections(int bitmask, int originalMask, byte[] data, boolean isOverworld) {
        ChunkSection[] sections = new ChunkSection[16];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        ByteBufferReader bbr = new ByteBufferReader(buffer);
        for (int i = 0; i < 16; i++) {
            if ((bitmask & 0x8000 >> (15 - i)) != 0) {
                if ((originalMask & 0x8000 >> (15 - i)) != 0) {
                    short nonAirCount = buffer.getShort();
                    short bpb = (short) Byte.toUnsignedInt(buffer.get());
                    int paletteLength = 0;
                    BlockPalette palette;
                    if(bpb <= 8) {
                        paletteLength = bbr.readVarInt();
                        int[] paletteData = new int[paletteLength];
                        for (int j = 0; j < paletteLength; j++) {
                            paletteData[j] = bbr.readVarInt();
                        }
                        palette = BlockPalette.createPalette(paletteData, bpb);
                    } else {
                        palette = BlockPalette.GLOBAL;
                    }
                    int dataLength = bbr.readVarInt();
                    byte[] blockData = new byte[dataLength * 8];
                    buffer.get(blockData);
                    sections[i] = new ChunkSection(blockData, nonAirCount, palette);
                }
                else {
                    sections[i] = new ChunkSection(isOverworld);
                }
            }
        }
        byte[] additional = new byte[data.length - buffer.position()];
        buffer.get(additional);
        return new ChunkData(sections, additional);
    }

    private static Map<Integer, Collection<FakeBlock>> toSectionMap(Collection<FakeBlock> chunkBlocks) {
        Map<Integer, Collection<FakeBlock>> sectionMap = new HashMap<>();
        for (FakeBlock fb : chunkBlocks) {
            int section = fb.getY() / 16;
            Collection<FakeBlock> blocks;
            if (!sectionMap.containsKey(section)) {
                blocks = new HashSet<>();
                sectionMap.put(section, blocks);
            } else {
                blocks = sectionMap.get(section);
            }
            blocks.add(fb);
        }
        return sectionMap;
    }
}
