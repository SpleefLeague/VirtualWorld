package com.spleefleague.virtualworld.api.implementation;

import org.bukkit.Material;

/**
 *
 * @author jonas
 */
public class BlockData {
    
    private Material type;
    private byte data;

    public BlockData(Material type, byte data) {
        this.type = type;
        this.data = data;
    }

    public Material getType() {
        return type;
    }

    public byte getData() {
        return data;
    }

    public void setType(Material type) {
        this.type = type;
    }

    public void setData(byte data) {
        this.data = data;
    }
    
    public BlockData copy() {
        return new BlockData(type, data);
    }
}
