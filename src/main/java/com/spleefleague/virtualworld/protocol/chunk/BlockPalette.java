/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.protocol.chunk;

import java.util.HashMap;
import org.bukkit.Material;

/**
 *
 * @author Jonas
 */
public abstract class BlockPalette {
    
    public static final BlockPalette GLOBAL = GlobalBlockPalette.instance();
    
    public abstract ChunkBlockData[] decode(byte[] data);
    public abstract ChunkBlockData[] getBlocks();
    public abstract int getBitsPerBlock();
    public abstract int getLength();
    public abstract int[] getPaletteData();
    public abstract byte[] encode(ChunkBlockData[] data);
    
    public static BlockPalette createPalette(int[] data, int bitsPerBlock) {
        return new EncodedBlockPalette(data, bitsPerBlock);
    }
    
    public static BlockPalette createPalette(ChunkBlockData[] data) {
        int bitsPerBlock = Math.max(32 - Integer.numberOfLeadingZeros(data.length - 1), 4);
        if(bitsPerBlock < 9) {
            return new EncodedBlockPalette(data, bitsPerBlock);
        }
        else {
            return GLOBAL;
        }
    }
    
    private static class GlobalBlockPalette extends BlockPalette {

        @Override
        public ChunkBlockData[] decode(byte[] data) {
            ProtocolLongArrayBitReader reader = new ProtocolLongArrayBitReader(data);
            ChunkBlockData[] bdata = new ChunkBlockData[4096];//Chunk section is 16x16x16
            for (int i = 0; i < bdata.length; i++) {
                byte damage = reader.readByte(4);
                int id = reader.readInt(9);
                bdata[i] = new ChunkBlockData(Material.getMaterial(id), damage);
            }
            return bdata;
        }
        
        public static GlobalBlockPalette instance() {
            return new GlobalBlockPalette();
        }
        
        @Override
        public ChunkBlockData[] getBlocks() {
            return null;
        }

        @Override
        public int getBitsPerBlock() {
            return 13;
        }

        @Override
        public byte[] encode(ChunkBlockData[] data) {
            byte[] array = new byte[6656];
            ProtocolLongArrayBitWriter writer = new ProtocolLongArrayBitWriter(array);
            for(ChunkBlockData block : data) {
                writer.writeByte(block.getDamage(), 4);
                writer.writeInt(block.getType().getId(), 9);
            }
            return array;
        }

        @Override
        public int getLength() {
            return 0;
        }

        @Override
        public int[] getPaletteData() {
            return new int[0];
        }
    }
    
    private static class EncodedBlockPalette extends BlockPalette {
        
        private final ChunkBlockData[] lookupTable;
        private final int bitsPerBlock;
        
        public EncodedBlockPalette(int[] data, int bitsPerBlock) {
            this.bitsPerBlock = bitsPerBlock;
            this.lookupTable = createLookupTable(data);
        }
        
        public EncodedBlockPalette(ChunkBlockData[] lookupTable, int bitsPerBlock) {
            this.lookupTable = lookupTable;
            this.bitsPerBlock = bitsPerBlock;
        }
        
        private ChunkBlockData[] createLookupTable(int[] data) {
            ChunkBlockData[] lookupTable = new ChunkBlockData[data.length];
            for(int i = 0; i < data.length; i++) {
                byte damage = (byte) (data[i] & 0xF);
                int id = data[i] >> 4;
                lookupTable[i] = new ChunkBlockData(Material.getMaterial(id), damage);
            }
            return lookupTable;
        }
        
        @Override
        public ChunkBlockData[] decode(byte[] data) {
            ProtocolLongArrayBitReader reader = new ProtocolLongArrayBitReader(data);
            ChunkBlockData[] array = new ChunkBlockData[4096];
            for(int i = 0; i < array.length; i++) {
                array[i] = lookupTable[reader.readShort(bitsPerBlock)];
            }
            return array;
        }

        @Override
        public ChunkBlockData[] getBlocks() {
            return lookupTable;
        }

        @Override
        public int getBitsPerBlock() {
            return bitsPerBlock;
        }

        @Override
        public byte[] encode(ChunkBlockData[] data) {
            byte[] array = new byte[512 * bitsPerBlock];
            ProtocolLongArrayBitWriter writer = new ProtocolLongArrayBitWriter(array);
            HashMap<ChunkBlockData, Integer> lookup = new HashMap<>();
            for(int i = 0; i < lookupTable.length; i++) {
                lookup.put(lookupTable[i], i);
            }
            for(ChunkBlockData block : data) {
                try {
                    writer.writeInt(lookup.get(block), bitsPerBlock);
                } catch(NullPointerException e) {
                    System.out.println("Error encoding data: " + block.getType());
                    throw e;
                }
            }
            return array;
        }

        @Override
        public int[] getPaletteData() {
            int[] data = new int[lookupTable.length];
            for(int i = 0; i < data.length; i++) {
                data[i] = lookupTable[i].getDamage() & 0xF;
                data[i] |= lookupTable[i].getType().getId() << 4;
            }
            return data;
        }
        
        @Override
        public int getLength() {
            return lookupTable.length;
        }
    }
}
