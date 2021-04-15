package com.ismaiiil.alliance.LandClaiming;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.JSON.ConfigLoader;
import com.ismaiiil.alliance.JSON.PlayerData;
import com.ismaiiil.alliance.JSON.PlayerJsonData;
import com.ismaiiil.alliance.Scoreboard.AllianceScoreboardManager;
import com.ismaiiil.alliance.Scoreboard.EnumObjective;
import com.ismaiiil.alliance.Scoreboard.EnumScore;
import com.ismaiiil.alliance.Utils.Tuple;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.ismaiiil.alliance.LandClaiming.CornerTypes.*;
import static com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;
import static org.bukkit.Bukkit.getServer;

public class AllianceRegionManager {
    private static AllianceRegionManager inst;
    private AlliancePlugin alliancePlugin;
    private RegionManager regionManager;
    private AllianceScoreboardManager allianceScoreboardManager;
    private WorldGuardPlugin worldGuardPlugin;
    private PlayerJsonData playerJsonData;
    private int defaultRadius;

    public HashMap<UUID, Multimap<String, Block>> borderHighlighterCache = new HashMap<>();
    public HashMap<UUID,Integer> borderHighlighterTasks = new HashMap<>();

    public HashMap<UUID, Corner> selectorCache = new HashMap<>();
    public HashMap<UUID, Integer> balanceChangeTasks = new HashMap<>();

    private AllianceRegionManager(){
        alliancePlugin = AlliancePlugin.getInstance();
        defaultRadius = alliancePlugin.radius;
        regionManager = alliancePlugin.defaultRegionManager;
        allianceScoreboardManager = alliancePlugin.allianceScoreboardManager;
        worldGuardPlugin = alliancePlugin.worldGuardPlugin;
        playerJsonData = alliancePlugin.playerJsonData;
    }

    public static AllianceRegionManager getInstance(){
        if(inst == null){
            inst = new AllianceRegionManager();
        }
        return inst;
    }

    // Main functionalities

    public void createDefaultRegion(Player player, Block targetBlock) {
        var localPlayer = worldGuardPlugin.wrapPlayer(player);
        var playerData = playerJsonData.players.get(player.getName());
        //check user balance before creating region
        int claimCost = ((defaultRadius *2) + 1) * ((defaultRadius *2) + 1);
        if(playerData.balance - claimCost < 0){
            player.sendMessage("You dnt have enough block balance to claim this area (" + ((defaultRadius *2) + 1) + "*"+ ((defaultRadius *2) + 1) + "blocks)");
            return;
        }

        var defaultRegion = generateDefaultRegion(player, localPlayer, playerData, targetBlock, defaultRadius);

        playerData.regionsCreated += 1;
        playerData.balance -= claimCost;
        playerData.usedBalance += claimCost;

        getServer().getScheduler().runTaskAsynchronously(alliancePlugin, () -> ConfigLoader.saveConfig(alliancePlugin.playerJsonData,alliancePlugin.playerJsonFile));

        if (getRegionsInRegion(defaultRegion).size() == 0){
            regionManager.addRegion(defaultRegion);
            allianceScoreboardManager.updateAllPlayerScores(player, EnumObjective.BALANCE);
            player.sendMessage( "Created default region at " +  targetBlock.getX() + ", " + targetBlock.getY()+ ", " + targetBlock.getZ() + " radius: " + defaultRadius);
        }else{
            player.sendMessage("You can't create a claim that overlaps with another claim");
        }
    }

    public void expandPlayerRegion(Player player, Block targetBlock) {
        var cachedCorner = selectorCache.get(player.getUniqueId());
        var _region = (ProtectedCuboidRegion) regionManager.getRegion(cachedCorner.regionId);
        var playerData = playerJsonData.players.get(player.getName());

        if (cachedCorner.oldCorner.equals(BlockVector2.at(targetBlock.getX(),targetBlock.getZ()))){
            player.sendMessage( "You can't select the same point to expand");
            resetSelectorCache(player);
            return;
        }

        if (_region !=null){
            var newCornerValue = getNewCorners(_region, targetBlock,cachedCorner);


            //make a copy of the region (avoid mutating existing regions)
            ProtectedCuboidRegion newRegion = new ProtectedCuboidRegion(_region.getId(),
                    newCornerValue._1.toBlockVector3(0),
                    newCornerValue._2.toBlockVector3(targetBlock.getWorld().getMaxHeight()));

            newRegion.copyFrom(_region);

            var oldArea = _region.volume() / (_region.getMaximumPoint().getBlockY() - _region.getMinimumPoint().getBlockY());
            var newArea = newRegion.volume() / (_region.getMaximumPoint().getBlockY() - _region.getMinimumPoint().getBlockY());

            var tempBalanceDifference = oldArea - newArea;
            var newBalance = playerData.balance + tempBalanceDifference;

            //calculate if new region is smaller than the allowed size

            var sides = calculateSidesLength(newRegion);

            if (sides._1 <  ((defaultRadius *2)) ||
                    sides._2 <  ((defaultRadius *2)) ){
                player.sendMessage("Region cannot be smaller than " + ((defaultRadius *2) + 1));
                resetSelectorCache(player);
                return;
            }

            if (newBalance < 0){
                player.sendMessage( "You don't have enough block balance to do that!");
            }

            if (getRegionsInRegion(newRegion,_region).size() == 0){
                regionManager.removeRegion(_region.getId());
                regionManager.addRegion(newRegion);
                playerData.balance = newBalance;
                allianceScoreboardManager.updatePlayerScore(player,EnumScore.BALANCE_CURRENT);
                getServer().getScheduler().runTaskAsynchronously(alliancePlugin, () -> ConfigLoader.saveConfig(alliancePlugin.playerJsonData,alliancePlugin.playerJsonFile));

                resetHighlightedBlocks(player);
                highlightRegionsForPlayer(player);

                player.sendMessage( "Region expanded to: " +  newCornerValue._1.toString() + ", " + newCornerValue._2.toString());
            }else{
                player.sendMessage("You can't modify this claim to overlap another claim");
            }

        }else{
            player.sendMessage( "The region you selected does not exist anymore");
        }

        resetSelectorCache(player);
    }

    public void calculateNewRegionCost(Player player) {
        var targetBlock = player.getTargetBlock(5);
        var cachedCorner = selectorCache.get(player.getUniqueId());
        if (cachedCorner == null || targetBlock == null){return;}
        var _region = (ProtectedCuboidRegion) regionManager.getRegion(cachedCorner.regionId);
        var playerData = playerJsonData.players.get(player.getName());

        var newCornerValue = getNewCorners(_region, targetBlock,cachedCorner);

        if (cachedCorner.oldCorner.equals(BlockVector2.at(targetBlock.getX(),targetBlock.getZ()))){
            playerData.tempBalanceChange = 0;
            allianceScoreboardManager.updatePlayerScore(player,EnumScore.BALANCE_CURRENT);
            return;
        }

        //make a copy of the region and calulate the volume (since its flat we will get an area)
        ProtectedCuboidRegion newRegion = new ProtectedCuboidRegion(_region.getId(),
                newCornerValue._1.toBlockVector3(0),
                newCornerValue._2.toBlockVector3(0));

        var oldArea = _region.volume() / (_region.getMaximumPoint().getBlockY() - _region.getMinimumPoint().getBlockY());
        var newArea = newRegion.volume();

        playerData.tempBalanceChange = oldArea - newArea;

        allianceScoreboardManager.updatePlayerScore(player, EnumScore.BALANCE_CURRENT);
    }

    public void highlightRegionsForPlayer(Player p) {
        //highlight edges of regions within a certain area
        int h_radius = 6;
        Location targetLocation = p.getLocation();

        var corners = getCornersAsBlockVector(targetLocation,h_radius,targetLocation.getBlockY()+h_radius, targetLocation.getBlockY()-h_radius);

        var targetRegion = new ProtectedCuboidRegion("temp",true, corners._1, corners._2);

        var regions = getRegionsInRegion(targetRegion);

        for (var region:regions) {
            if (!region.isOwner(alliancePlugin.worldGuardPlugin.wrapPlayer(p))){
                continue;
            }
            //System.out.println(region.getId());
            var min = region.getMinimumPoint();
            var max = region.getMaximumPoint();

            var minFlat= BlockVector3.at(min.getBlockX(),targetLocation.getY(),  min.getBlockZ());
            var maxFlat= BlockVector3.at(max.getBlockX(),targetLocation.getY(), max.getBlockZ());

            var cuboid = new CuboidRegion(BukkitAdapter.adapt(p.getWorld()),minFlat,maxFlat);

            var walls = cuboid.getWalls();

            Multimap<String, Block> tempBlocks = ArrayListMultimap.create();
            for (var block:walls ) {
                var _y  = p.getWorld().getHighestBlockYAt(block.getBlockX(), block.getBlockZ());
                var highlightLocation = new Location(p.getWorld(), block.getBlockX(),_y,block.getBlockZ() );

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.sendBlockChange(highlightLocation, Material.GOLD_BLOCK.createBlockData());
                    }
                }.runTaskAsynchronously(alliancePlugin);

                tempBlocks.put(region.getId() , p.getWorld().getBlockAt(highlightLocation));

            }

            if (borderHighlighterCache.containsKey(p.getUniqueId())){
                Multimap<String, Block> tempCache = borderHighlighterCache.get(p.getUniqueId());
                tempCache.putAll(tempBlocks);
            }else{
                borderHighlighterCache.put(p.getUniqueId(),tempBlocks);
            }

        }
    }

    public void promptUserToDelete(Player player, ProtectedRegion region) {
        final TextComponent textComponent = Component.text("Do you want to delete this region? Click here to delete >>>")
                .append(Component.text("Yes").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,"/rg rem "+region.getId()))
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.BOLD,true)
                )
                ;
        player.sendMessage(textComponent);

    }

    //Helper methods

    public ProtectedRegion generateDefaultRegion(Player player, LocalPlayer localPlayer, PlayerData playerData, Block targetBlock,int radius) {
        //target block is never null because of RIGHT_CLICK_BLOCK Action
        var targetLocation = targetBlock.getLocation();

        var corners = getCornersAsBlockVector(targetLocation,radius, 0, player.getWorld().getMaxHeight());

        var defaultRegion = new ProtectedCuboidRegion("region_"+ player.getName()+ "_" + playerData.regionsCreated,corners._1, corners._2);

        defaultRegion.getOwners().addPlayer(localPlayer);
        defaultRegion.setFlag(Flags.USE, DENY);
        defaultRegion.setFlag(Flags.TRAMPLE_BLOCKS, DENY);
        defaultRegion.setFlag(Flags.FIRE_SPREAD, DENY);
        return defaultRegion;
    }

    public Tuple<BlockVector3, BlockVector3> getCornersAsBlockVector(Location targetLocation, int radius, int minY, int maxY){
        Location corner1Location = new Location(targetLocation.getWorld(), targetLocation.getX() + radius, maxY, targetLocation.getZ() + radius );
        Location corner2Location = new Location(targetLocation.getWorld(), targetLocation.getX() - radius,minY, targetLocation.getZ() - radius );

        BlockVector3 corner1Vector3 = BukkitAdapter.asBlockVector(corner1Location);
        BlockVector3 corner2Vector3 = BukkitAdapter.asBlockVector(corner2Location);

        return new Tuple<>(corner1Vector3, corner2Vector3);
    }

    public void stopPlayerHighlighterTasks(Player p) {
        if (borderHighlighterTasks.containsKey(p.getUniqueId())) {
            AlliancePlugin.getInstance().getServer().getScheduler().cancelTask(borderHighlighterTasks.get(p.getUniqueId()));
            borderHighlighterTasks.remove(p.getUniqueId());
        }
    }

    public void resetHighlightedBlocks(Player p) {
        if (borderHighlighterCache.containsKey(p.getUniqueId())){
            var tempBlocks = borderHighlighterCache.get(p.getUniqueId());
            for ( var block : tempBlocks.values()){
                getServer().getScheduler().runTaskAsynchronously(alliancePlugin, () -> {
                    block.getState().update();
                });
            }
            borderHighlighterCache.remove(p.getUniqueId());
        }
    }

    public void resetBalanceChange(Player p) {
        var playerData = playerJsonData.players.get(p.getName());
        balanceChangeTasks.remove(p.getUniqueId());
        playerData.tempBalanceChange = 0;
        allianceScoreboardManager.updatePlayerScore(p,EnumScore.BALANCE_CURRENT);
    }

    public void stopBalanceChangeTasks(Player p) {
        if (balanceChangeTasks.containsKey(p.getUniqueId())) {
            AlliancePlugin.getInstance().getServer().getScheduler().cancelTask(balanceChangeTasks.get(p.getUniqueId()));
            balanceChangeTasks.remove(p.getUniqueId());
        }
    }

    public void resetSelectorCache(Player p) {
        selectorCache.remove(p.getUniqueId());
    }

    public CornerTypes determineCorner(ProtectedRegion region, Block selectedBlock){
        var min =  region.getMinimumPoint().toBlockVector2();
        var max =  region.getMaximumPoint().toBlockVector2();
        var selectedCorner = asBlockVector2(selectedBlock);

        var xMin = min.getBlockX();
        var zMin = min.getBlockZ();
        var xMax = max.getBlockX();
        var zMax = max.getBlockZ();

        if (selectedCorner.getBlockX() == xMin && selectedCorner.getBlockZ() == zMin){
            return SOUTH_WEST;
        }else if(selectedCorner.getBlockX() == xMin && selectedCorner.getBlockZ() == zMax){
            return SOUTH_EAST;
        }else if(selectedCorner.getBlockX() == xMax && selectedCorner.getBlockZ() == zMin){
            return NORTH_WEST;
        }else if(selectedCorner.getBlockX() == xMax && selectedCorner.getBlockZ() == zMax){
            return NORTH_EAST;
        }
        return null;
    }

    public Tuple<BlockVector2, BlockVector2> getNewCorners(ProtectedRegion region, Block selectedBlock, Corner corner){
        var min =  region.getMinimumPoint().toBlockVector2();
        var max =  region.getMaximumPoint().toBlockVector2();
        var newCorner = asBlockVector2(selectedBlock);
        var oldCorner = corner.oldCorner;
        var xMin_old = min.getBlockX();
        var zMin_old = min.getBlockZ();
        var xMax_old = max.getBlockX();
        var zMax_old = max.getBlockZ();

        BlockVector2 displacement = newCorner.subtract(oldCorner);;
        var XDiff = displacement.getBlockX();
        var ZDiff = displacement.getBlockZ();


        switch (corner.cornerTypes){
            case SOUTH_WEST:
                return new Tuple<>(newCorner, max);
            case SOUTH_EAST:
                return  new Tuple<>(BlockVector2.at(xMin_old + XDiff, zMin_old ), BlockVector2.at(xMax_old, zMax_old + ZDiff ));
            case NORTH_WEST:
                return  new Tuple<>(BlockVector2.at(xMin_old, zMin_old + ZDiff), BlockVector2.at(xMax_old + XDiff, zMax_old));
            case NORTH_EAST:
                return  new Tuple<>(min, newCorner);
        }
        return null;
    }

    public BlockVector2 asBlockVector2(Block block){
        return BlockVector2.at(block.getX(),block.getZ());
    }

    public Set<ProtectedRegion> getRegionsInRegion(ProtectedRegion region){
        var regions = regionManager.getApplicableRegions(region).getRegions();
        regions.remove(region);

        return regions;
    }

    /**
     * get the regions inside a region
     * this is a variant of getRegionsInRegion(ProtectedRegion region)
     * used we have an old region and still want to check for other regions
     * but excluding that oldRegion
     * @param region the region in memory we are using to check for regions
     * @param oldRegion the region that we are going to copy
     * @return the regions in the region
     */
    public Set<ProtectedRegion> getRegionsInRegion(ProtectedRegion region, ProtectedRegion oldRegion){

        //oldRegionis used, in case we have an old region and still want to check for other regions
        //but excluding that oldRegion
        var regions = regionManager.getApplicableRegions(region).getRegions();
        regions.remove(region);
        regions.remove(oldRegion);

        return regions;
    }

    public boolean selectFirstCornerBlock(Player player, Block targetBlock, ArrayList<ProtectedRegion> ownedRegions) {
        var clickedRegion = ownedRegions.get(0);
        //check if block clicked is a corner block
        var cornerType = determineCorner(clickedRegion, targetBlock);

        if (cornerType != null){
            var selectorCacheValues = new Corner(clickedRegion.getId(), targetBlock,cornerType);
            selectorCache.put(player.getUniqueId(),selectorCacheValues);
            player.sendMessage( "Corner " + selectorCacheValues.cornerTypes + " selected for expansion");
            return true;
        }
        return false;
    }

    /**
     * Calculate sides for a region
     * @param region the region we want to calculate
     * @return HorizontalSide, VerticalSide
     */
    public Tuple<Integer,Integer> calculateSidesLength(ProtectedRegion region){
        //calculate if new region is smaller than the allowed size
        var newMax = region.getMaximumPoint();
        var newMin = region.getMinimumPoint();

        var horizontalSide = Math.abs(newMax.getBlockX() - newMin.getBlockX());
        var verticalSide = Math.abs(newMax.getBlockZ() - newMin.getBlockZ());

        return new Tuple<>(horizontalSide,verticalSide);
    }


}
