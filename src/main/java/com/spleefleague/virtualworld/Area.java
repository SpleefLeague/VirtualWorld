package com.spleefleague.virtualworld;

import org.bukkit.util.Vector;

/**
 *
 * @author balsfull
 */
public class Area {
    
    private final Vector high, low;
    
    public Area(Vector loc1, Vector loc2) {
        low = new Vector(Math.min(loc1.getX(), loc2.getX()), Math.min(loc1.getY(), loc2.getY()), Math.min(loc1.getZ(), loc2.getZ()));
        high = new Vector(Math.max(loc1.getX(), loc2.getX()), Math.max(loc1.getY(), loc2.getY()), Math.max(loc1.getZ(), loc2.getZ()));
    }

    public Vector getHigh() {
        return high;
    }

    public Vector getLow() {
        return low;
    }
    
    public boolean isInsideX(double x) {
        return low.getX() <= x && x <= high.getX();
    }
    
    public boolean isInsideY(double y) {
        return low.getY() <= y && y <= high.getY();
    }
    
    public boolean isInsideZ(double z) {
        return low.getZ() <= z && z <= high.getZ();
    }
    
    public boolean isInside(Vector loc) {
        return isInsideX(loc.getX()) && isInsideY(loc.getY()) && isInsideZ(loc.getZ());
    }
    
    @Override
    public String toString() {
        return "{" + low.toString() + ", " + high.toString() + "}";
    }
}
