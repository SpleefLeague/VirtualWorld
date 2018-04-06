/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.virtualworld.api;

import com.spleefleague.virtualworld.Area;
import java.util.Collection;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 *
 * @author jonas
 */
public interface FakeWorld {
    
    FakeChunk getChunkAt(int x, int z);
    FakeBlock getBlockAt(int x, int y, int z);
    FakeBlock getBlockAt(Location loc);
    FakeBlock getBlockAt(Vector vec);
    Collection<? extends FakeBlock> getUsedBlocks(); 
    World getHandle();
    Area getArea();
    
    void playEffect(Location lctn, Effect effect, int i);
    <T extends Object> void playEffect(Location lctn, Effect effect, T t);
    
    void spawnParticle(Particle particle, Location location, int count);
    <T> void spawnParticle(Particle particle, Location location, int count, T data);
    void spawnParticle(Particle particle, Location location, int count, double xOffset, double yOffset, double zOffset);
    <T> void spawnParticle(Particle particle, Location location, int count, double xOffset, double yOffset, double zOffset, T data);
    void spawnParticle(Particle particle, Location location, int count, double xOffset, double yOffset, double zOffset, double extra);
    <T> void spawnParticle(Particle particle, Location location, int count, double xOffset, double yOffset, double zOffset, double extra, T data);
    
    <T> void playSound(Location location, Sound sound, float volume, float pitch);
}
