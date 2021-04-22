package com.ismaiiil.alliance.features.rtp;

import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.features.threadpool.ThreadManager;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.var;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class RTPManager {
    private static AlliancePlugin alliancePlugin;
    private static RegionManager regionManager;

    private static ConcurrentHashMap<UUID, TPLink> playerToTpLinks = new ConcurrentHashMap<>();

    public static void init(){
        alliancePlugin = AlliancePlugin.getInstance();
        regionManager = alliancePlugin.defaultRegionManager;
        //Schedule a task to automatically delete player TP links after a delay
        new BukkitRunnable() {
            @Override
            public void run() {
                deleteExpiredTpLinks();
            }
        }.runTaskTimerAsynchronously(alliancePlugin, 0L, 20L);
    }

    public static CompletableFuture<TPLink> rtp(Player p) {
        int radius = alliancePlugin.rtpRadius;

        Location spawn = p.getWorld().getSpawnLocation();
        Location destination = generateRandomSpawn(radius, spawn);
        final CompletableFuture<TPLink> promise = new CompletableFuture<>();
        var future = CompletableFuture.supplyAsync(() -> lookupRandomLocation(destination, p, radius, spawn, 1), ThreadManager.pool)
        .thenApply(location -> {
            ThreadManager.runMainThread(() -> {
                teleportPlayer(location,p);
                p.setBedSpawnLocation(location, true);
                var tpLink = new TPLink(p, location);
                playerToTpLinks.remove(p.getUniqueId());
                p.sendMessage(tpLink.buildTPLinkShareMessage());
                playerToTpLinks.put(p.getUniqueId(), tpLink);
                promise.complete(tpLink);
                return null;
            });
            return null;
        });

        future.exceptionally(throwable -> {
            throwable.printStackTrace();
            promise.completeExceptionally(throwable);
            if (throwable.getCause() instanceof MaxRecursiveCountExceededException){
                ThreadManager.runMainThread(() -> {
                    p.kick(Component.text("Not enough space on the map to rtp you"));
                    return null;
                });
            }
            return null;
        });

        return promise;
    }

    public static void tpUsingTpLink(Player player, String tpID){
        for(var tpLink: playerToTpLinks.values()){
            if (tpLink.linkID.equals(tpID)){
                if(tpLink.createdBy.equals(player.getUniqueId())){
                    player.sendMessage(Component.text("You can't TP to your own TP link"));
                    return;
                }
                if (tpLink.listUsedBy.contains(player.getUniqueId())){
                    player.sendMessage(Component.text("You already used this TP link"));
                    return;
                }
                tpLink.listUsedBy.add(player.getUniqueId());
                player.teleport(tpLink.destination);
                return;
            }
        }
        player.sendMessage(Component.text("TP link is invalid or has expired"));


    }

    public static Location generateRandomSpawn(int radius, Location spawn){
        int x = spawn.getBlockX();
        int z = spawn.getBlockZ();
        int maxX= x + radius;
        int minX= x - radius;
        int maxZ= z + radius;
        int minZ= z - radius;
        Random Xrand = new Random();
        x = Xrand.nextInt(maxX - minX) + minX;
        Random Zrand = new Random();
        z = Zrand.nextInt(maxZ - minZ) + minZ;

        World world = Bukkit.getServer().getWorld("world");
        int y = world.getHighestBlockYAt(x,z);

        Location destination = new Location(world, x, y+1, z);

        return destination;
    }

    public static void teleportPlayer(Location destination, Player player){
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String command = "tp " + player.getName() + " " + destination.getX()  + " " + destination.getY() + " " + destination.getZ();
        Bukkit.dispatchCommand(console, command);
    }

    public static Location lookupRandomLocation(Location destination, Player player, int radius, Location spawn, int recursiveCount){
        World world;
        if ((world = player.getLocation().getWorld()) == null){
            return null;
        }
        Location blockBelow = new Location( world, destination.getX(), destination.getY()-1, destination.getZ());

        Block block = world.getBlockAt(blockBelow);
        Block block1 = world.getBlockAt(destination);

        boolean moveNext = false;

        Set<ProtectedRegion> regionsAtTarget = regionManager.getApplicableRegions(
                BlockVector3.at(blockBelow.getBlockX(),blockBelow.getBlockY(),blockBelow.getBlockZ()))
                .getRegions();

        if (regionsAtTarget.size() > 0){
            moveNext = true;
            for (ProtectedRegion region: regionsAtTarget) {
                if (region.isOwner(WorldGuardPlugin.inst().wrapPlayer(player))) {
                    moveNext = false;
                    break;
                }
            }
        }

        if (block.getType() == Material.LAVA || block.getType() == Material.WATER ||
                block1.getType() == Material.LAVA || block1.getType() == Material.WATER ||
                moveNext
        ){
            Location location = generateRandomSpawn(radius, spawn);

            if (recursiveCount+1 > alliancePlugin.maxLookupCount){
                throw new MaxRecursiveCountExceededException();
            }

            return lookupRandomLocation(location, player, radius, spawn, recursiveCount + 1);
        }
        return destination;
    }

    public static void deleteExpiredTpLinks(){
        for (var tpLink: playerToTpLinks.entrySet()) {
            if (tpLink.getValue().hasExpired()){
                playerToTpLinks.remove(tpLink.getKey());
            }
        }
    }




}
