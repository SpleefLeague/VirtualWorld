/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.protocol.chunk;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.RegistryBlockID;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;

/**
 *
 * @author Jonas
 */
public abstract class BlockPalette {
    
    public static final BlockPalette GLOBAL = GlobalBlockPalette.instance();
    private static final RegistryBlockID<IBlockData> REGISTRY_ID = Block.REGISTRY_ID;
    
    public abstract BlockData[] decode(byte[] data);
    public abstract BlockData[] getBlocks();
    public abstract int getBitsPerBlock();
    public abstract int getLength();
    public abstract int[] getPaletteData();
    public abstract byte[] encode(BlockData[] data);
    public abstract boolean includePaletteLength();
    
    public static int blockDataToId(BlockData data) {
        return REGISTRY_ID.getId(((CraftBlockData)data).getState());
    }
    
    public static BlockData blockDataFromId(int id) {
        IBlockData ibd = REGISTRY_ID.fromId(id);
        return CraftBlockData.fromData(ibd);
    }
    
    public static BlockPalette createPalette(int[] data, int bitsPerBlock) {
        return new EncodedBlockPalette(data, bitsPerBlock);
    }
    
    public static BlockPalette createPalette(BlockData[] data) {
        int bitsPerBlock = Math.max(32 - Integer.numberOfLeadingZeros(data.length - 1), 4);
        if(bitsPerBlock <= 8) {
            return new EncodedBlockPalette(data, bitsPerBlock);
        }
        else {
            return GLOBAL;
        }
    }
    
    private static class GlobalBlockPalette extends BlockPalette {

        @Override
        public BlockData[] decode(byte[] data) {
            ProtocolLongArrayBitReader reader = new ProtocolLongArrayBitReader(data);
            BlockData[] bdata = new BlockData[4096];//Chunk section is 16x16x16
            for (int i = 0; i < bdata.length; i++) {
                int id = reader.readInt(14);
                bdata[i] = blockDataFromId(id);
            }
            return bdata;
        }
        
        public static GlobalBlockPalette instance() {
            return new GlobalBlockPalette();
        }
        
        @Override
        public BlockData[] getBlocks() {
            return null;
        }

        @Override
        public int getBitsPerBlock() {
            return 14;
        }
        
        @Override
        public boolean includePaletteLength() {
            return false;
        }

        @Override
        public byte[] encode(BlockData[] data) {
            byte[] array = new byte[512 * getBitsPerBlock()];
            ProtocolLongArrayBitWriter writer = new ProtocolLongArrayBitWriter(array);
            for(BlockData block : data) {
                writer.writeInt(blockDataToId(block), 14);
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
        
        private final BlockData[] lookupTable;
        private final int bitsPerBlock;
        
        public EncodedBlockPalette(int[] data, int bitsPerBlock) {
            this.bitsPerBlock = bitsPerBlock;
            this.lookupTable = createLookupTable(data);
        }
        
        public EncodedBlockPalette(BlockData[] lookupTable, int bitsPerBlock) {
            this.lookupTable = lookupTable;
            this.bitsPerBlock = bitsPerBlock;
        }
        
        private BlockData[] createLookupTable(int[] data) {
            BlockData[] lookupTable = new BlockData[data.length];
            for(int i = 0; i < data.length; i++) {
                lookupTable[i] = blockDataFromId(data[i]);
            }
            return lookupTable;
        }
        
        @Override
        public BlockData[] decode(byte[] data) {
            ProtocolLongArrayBitReader reader = new ProtocolLongArrayBitReader(data);
            BlockData[] array = new BlockData[4096];
            for(int i = 0; i < array.length; i++) {
                array[i] = lookupTable[reader.readShort(bitsPerBlock)];
            }
            return array;
        }

        @Override
        public BlockData[] getBlocks() {
            return lookupTable;
        }

        @Override
        public int getBitsPerBlock() {
            return bitsPerBlock;
        }
        
        @Override
        public boolean includePaletteLength() {
            return true;
        }

        @Override
        public byte[] encode(BlockData[] data) {
            byte[] array = new byte[512 * bitsPerBlock];//16^3 / 8
            ProtocolLongArrayBitWriter writer = new ProtocolLongArrayBitWriter(array);
            Map<BlockData, Integer> lookup = new HashMap<>();
            for(int i = 0; i < lookupTable.length; i++) {
                lookup.put(lookupTable[i], i);
            }
            for(BlockData block : data) {
                try {
                    writer.writeInt(lookup.get(block), bitsPerBlock);
                } catch(NullPointerException e) {
                    System.out.println("Error encoding data: " + block.getAsString());
                    throw e;
                }
            }
            return array;
        }

        @Override
        public int[] getPaletteData() {
            int[] data = new int[lookupTable.length];
            for(int i = 0; i < data.length; i++) {
                data[i] = blockDataToId(lookupTable[i]);
            }
            return data;
        }
        
        @Override
        public int getLength() {
            return lookupTable.length;
        }
    }
} 
