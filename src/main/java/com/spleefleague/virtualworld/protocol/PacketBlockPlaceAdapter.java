package com.spleefleague.virtualworld.protocol;

import com.comphenix.packetwrapper.WrapperPlayClientBlockPlace;
import com.comphenix.packetwrapper.WrapperPlayClientUseItem;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;
import com.spleefleague.virtualworld.FakeWorldManager;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.api.FakeWorld;
import com.spleefleague.virtualworld.api.implementation.BlockChange;
import com.spleefleague.virtualworld.api.implementation.FakeBlockBase;
import com.spleefleague.virtualworld.event.FakeBlockPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 *
 * @author balsfull
 */
public class PacketBlockPlaceAdapter extends PacketAdapter {

    private final FakeWorldManager fwmanager;

    public PacketBlockPlaceAdapter(FakeWorldManager fwmanager) {
        super(VirtualWorld.getInstance(), ListenerPriority.NORMAL, new PacketType[]{PacketType.Play.Client.USE_ITEM});
        this.fwmanager = fwmanager;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        WrapperPlayClientUseItem wrapper = new WrapperPlayClientUseItem(event.getPacket());
        Vector direction = new Vector(0, 0, 0);
        switch(wrapper.getFace()) {
            case UP: direction.setY(1); break;
            case DOWN: direction.setY(-1); break;
            case NORTH: direction.setZ(-1); break;
            case SOUTH: direction.setZ(1); break;
            case WEST: direction.setX(-1); break;
            case EAST: direction.setX(1); break;
        }
        Location placeLocation = wrapper
                .getLocation()
                .toVector()
                .add(direction)
                .toLocation(player.getWorld());
        ItemStack handItem;
        if(wrapper.getHand() == Hand.MAIN_HAND) {
            handItem = player.getInventory().getItemInMainHand();
        }
        else {
            handItem = player.getInventory().getItemInOffHand();
        }
        if(!handItem.getType().isBlock()) {
            return;
        }
        FakeWorld targetWorld = fwmanager.getWorldAt(player, placeLocation);
        if(targetWorld != null) {
            event.setCancelled(true);
            FakeBlockPlaceEvent placeEvent = new FakeBlockPlaceEvent(player, targetWorld.getBlockAt(placeLocation), handItem.getType(), handItem.getData().getData());
            Bukkit.getScheduler().runTask(VirtualWorld.getInstance(), () -> {
                Bukkit.getPluginManager().callEvent(placeEvent);
                if(placeEvent.isCancelled()) {
                    Bukkit.getScheduler().runTaskLater(VirtualWorld.getInstance(), () -> {
                        player.sendBlockChange(new Location(player.getWorld(), placeLocation.getX(), placeLocation.getY(), placeLocation.getZ()), Material.AIR, (byte)0);
                    }, 1);
                    return;
                }
                FakeBlockBase eventBlock = (FakeBlockBase)placeEvent.getBlock();
                eventBlock._setType(handItem.getType());
                eventBlock._setData(handItem.getData().getData());
                eventBlock.registerChanged(BlockChange.ChangeType.PLACE);
                if(player.getGameMode() != GameMode.CREATIVE) {
                    handItem.setAmount(handItem.getAmount() - 1);
                }
            });
        }
        else {
            Bukkit.getScheduler().runTaskLater(VirtualWorld.getInstance(), () -> {
                player.sendBlockChange(new Location(player.getWorld(), placeLocation.getX(), placeLocation.getY(), placeLocation.getZ()), Material.AIR, (byte)0);
            }, 1);
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {

    }
    
    //Uses raytracing to find the location where the player intents to place a block.
    //Returns null if no location has been found
    public Location getPlaceLocation(Player player, int radius) {
        World world = player.getWorld();
        Vector origin = player.getLocation().toVector();
        Vector direction = player.getLocation().getDirection();
        double x = Math.floor(origin.getX());
        double y = Math.floor(origin.getY());
        double z = Math.floor(origin.getZ());
        double dx = direction.getX();
        double dy = direction.getY();
        double dz = direction.getZ();
        double stepX = signum(dx);
        double stepY = signum(dy);
        double stepZ = signum(dz);
        double tMaxX = intbound(origin.getX(), dx);
        double tMaxY = intbound(origin.getY(), dy);
        double tMaxZ = intbound(origin.getZ(), dz);

        double tDeltaX = stepX / dx;
        double tDeltaY = stepY / dy;
        double tDeltaZ = stepZ / dz;
        Vector face = new Vector(0, 3, 0);

        if (dx == 0 && dy == 0 && dz == 0) {
            throw new UnsupportedOperationException("Raycast in zero direction!");
        }
        radius /= Math.sqrt(dx * dx + dy * dy + dz * dz);

        while (true) {
            Location loc = new Location(world, x, y, z);
            FakeBlock fb = fwmanager.getBlockAt(player, loc);
            //Transparent fakeblocks (AIR) have higher precedence than existing normal blocks.
            if(fb != null) {
                if(!fb.getType().isTransparent()) {
                    return loc.add(face.normalize());
                }
            }
            else if(!world.getBlockAt(loc).getType().isTransparent()) {
                return loc.add(face.normalize());
            }
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    if (tMaxX > radius) {
                        break;
                    }
                    x += stepX;
                    tMaxX += tDeltaX;
                    face.setX(-stepX);
                    face.setY(0);
                    face.setZ(0);
                }
                else {
                    if (tMaxZ > radius) {
                        break;
                    }
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                    face.setX(0);
                    face.setY(0);
                    face.setZ(-stepZ);
                }
            }
            else if (tMaxY < tMaxZ) {
                if (tMaxY > radius) {
                    break;
                }
                y += stepY;
                tMaxY += tDeltaY;
                face.setX(0);
                face.setY(-stepY);
                face.setZ(0);
            }
            else {
                if (tMaxZ > radius) {
                    break;
                }
                z += stepZ;
                tMaxZ += tDeltaZ;
                face.setX(0);
                face.setY(0);
                face.setZ(-stepZ);
            }
        }
        return null;
    }

    public double intbound(double s, double ds) {
        if (ds < 0) {
            return intbound(-s, -ds);
        }
        else {
            s = mod(s, 1);
            return (1 - s) / ds;
        }
    }

    public double signum(double x) {
        return x > 0 ? 1 : x < 0 ? -1 : 0;
    }

    public double mod(double value, double modulus) {
        return (value % modulus + modulus) % modulus;
    }

    private boolean vectorEqual(Vector loc1, Vector loc2) {
        if ((loc1.getX() + 0.5) / 1.0 == (loc2.getX() + 0.5) / 1.0 && (loc1.getZ() + 0.5) / 1.0 == (loc2.getZ() + 0.5) / 1.0 && loc1.getY() / 1.0 == loc2.getY() / 1.0) {
            return true;
        }
        return false;
    }
}