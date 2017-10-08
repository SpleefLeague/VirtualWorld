/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.protocol.chunk;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;

/**
 *
 * @author Jonas
 */
public class ChunkSection {
    
    private final ChunkBlockData[] blocks;
    private final byte[] lightData;
    private boolean modified = false;
    private ChunkBlockData[] paletteBlocks;
    private final Set<ChunkBlockData> paletteBlockSet;
    
    /**
     * 
     * @param blockdata Block data array described as in http://wiki.vg/SMP_Map_Format
     * @param lightData The chunk's original lighting data
     * @param palette Block palette object
     */
    public ChunkSection(byte[] blockdata, byte[] lightData, BlockPalette palette) {
        this.blocks = palette.decode(blockdata);
        paletteBlocks = palette.getBlocks();//Null for the global palette
        if(paletteBlocks != null) {
            paletteBlockSet = new HashSet<>();
            for(ChunkBlockData data : paletteBlocks) {
                paletteBlockSet.add(data);
            }
        }
        else {
            paletteBlockSet = null;
        }
        this.lightData = lightData;
    }
    
    protected ChunkSection(boolean overworld) {
        ChunkBlockData air = new ChunkBlockData(Material.AIR, (byte)0);
        blocks = new ChunkBlockData[4096];
        Arrays.fill(blocks, air);
        //An empty, unsent chunksection contains air blocks
        paletteBlocks = new ChunkBlockData[]{air};
        paletteBlockSet = new HashSet<>();
        paletteBlockSet.add(air);
        lightData = new byte[overworld ? 4096 : 2048];
        Arrays.fill(lightData, (byte)-1);//Default light data, everything is bright
    }
    
    public ChunkBlockData getBlockRelative(int x, int y, int z) {
        return blocks[x + z * 16 + y * 256];
    }
    
    public void setBlockRelative(ChunkBlockData data, int x, int y, int z) {
        blocks[x + z * 16 + y * 256] = data;
        modified = true;
        if(paletteBlockSet != null) {
            if(!paletteBlockSet.contains(data)) {
                paletteBlocks = null;
            }
            paletteBlockSet.add(data);
        }
    }
    
    public ChunkBlockData[] getBlockData() {
        return blocks;
    }
    
    public boolean isModified() {
        return modified;
    }
    
    public ChunkBlockData[] getContainedBlocks() {
        if(paletteBlocks == null) {
            if(paletteBlockSet == null) {
                return null;
            }
            paletteBlocks = paletteBlockSet.toArray(new ChunkBlockData[0]);
        }
        return paletteBlocks;
    }
    
    public byte[] getLightingData() {
        return lightData;
    }
}
