package com.ismaiiil.alliance.features.rtp;

import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.features.threadpool.ThreadManager;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class RTPManager {
    private static AlliancePlugin alliancePlugin;
    private static RegionManager regionManager;

    private static ConcurrentHashMap<UUID, TPLink> playerToTpLinks = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<UUID, Long> playerToTimeRTP = new ConcurrentHashMap<>();
    //TODO
    //investigate thread safety
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

        if (playerToTimeRTP.containsKey(p.getUniqueId())){
            var timeCreated = playerToTimeRTP.get(p.getUniqueId());
            if ((System.currentTimeMillis() - timeCreated) < alliancePlugin.rtpCoolDown){
                var timeLeft = (alliancePlugin.rtpCoolDown - (System.currentTimeMillis() - timeCreated));
                long minutes = (timeLeft / 1000) / 60;
                long seconds = (timeLeft / 1000) % 60;
                p.sendMessage(String.format("You can't use /rtp yet, on cooldown, %s minutes and %s seconds left", minutes,seconds));
                return promise;
            }else{
                playerToTimeRTP.remove(p.getUniqueId());
            }

        }

        var future = CompletableFuture.supplyAsync(() -> {
            p.sendMessage("Please wait, lookin' for safe location!");
            playerToTimeRTP.put(p.getUniqueId(), System.currentTimeMillis());
            return loopWhileInvalidLocation(destination, p, radius, spawn);
        }, ThreadManager.pool)
        .thenApply(location -> {
            ThreadManager.runMainThread(() -> {
                teleportPlayer(location,p);
                p.setBedSpawnLocation(location, true);

                var tpLink = new TPLink(p, location);
                playerToTpLinks.remove(p.getUniqueId());
                playerToTpLinks.put(p.getUniqueId(), tpLink);

                p.sendMessage(tpLink.buildTPLinkShareMessage());
                promise.complete(tpLink);
                return null;
            });
            return null;
        });

        future.exceptionally(throwable -> {
            throwable.printStackTrace();

            if (throwable.getCause() instanceof MaxLookupCountExceeded){
                ThreadManager.runMainThread(() -> {
                    promise.completeExceptionally(throwable);
                    p.sendMessage(Component.text("Not enough space on the map to rtp you"));
                    return null;
                });
            }else{
                p.sendMessage(Component.text("An unknown error has occurred, please report to Server admin")
                                        .color(NamedTextColor.RED));
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
        player.teleport(destination);
        Bukkit.getServer().getConsoleSender().sendMessage(String.format("Player: %s has been rtped to %s, %s, %s",
                                                                        player.getName(), destination.getBlockX(), destination.getBlockY(), destination.getBlockZ()));

    }

    public static Location checkValidLocation(Location destination, Player player){
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
            return null;
        }
        return destination;
    }

    public static Location loopWhileInvalidLocation(Location destination, Player player, int radius, Location spawn){
        int lookupCount = 1;
        Location finalDestination;
        Location newLocation  = destination;

        do {
            finalDestination = checkValidLocation(newLocation, player);
            lookupCount += 1;
            newLocation = generateRandomSpawn(radius, spawn);

            if (lookupCount+1 > alliancePlugin.maxLookupCount){
                throw new MaxLookupCountExceeded();
            }

        } while (finalDestination == null);

        return finalDestination;
    }

    public static void deleteExpiredTpLinks(){
        for (var tpLink: playerToTpLinks.entrySet()) {
            if (tpLink.getValue().hasExpired()){
                playerToTpLinks.remove(tpLink.getKey());
            }
        }
    }


}
