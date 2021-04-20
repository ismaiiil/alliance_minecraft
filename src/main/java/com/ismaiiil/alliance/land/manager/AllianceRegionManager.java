package com.ismaiiil.alliance.land.manager;

import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.json.ConfigLoader;
import com.ismaiiil.alliance.json.PlayerData;
import com.ismaiiil.alliance.json.PlayerJsonData;
import com.ismaiiil.alliance.land.util.CallbackForEachBlock;
import com.ismaiiil.alliance.land.cache.implemented.BalanceCache;
import com.ismaiiil.alliance.land.cache.implemented.BorderCache;
import com.ismaiiil.alliance.land.cache.implemented.SelectorCache;
import com.ismaiiil.alliance.land.corner.Corner;
import com.ismaiiil.alliance.land.corner.CornerTypes;
import com.ismaiiil.alliance.scoreboard.AllianceScoreboardManager;
import com.ismaiiil.alliance.scoreboard.EnumObjective;
import com.ismaiiil.alliance.scoreboard.EnumScore;
import com.ismaiiil.alliance.utils.Tuple;
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
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static com.ismaiiil.alliance.land.corner.CornerTypes.*;
import static com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;
import static org.bukkit.Bukkit.getServer;

public class AllianceRegionManager {
    private static AlliancePlugin alliancePlugin;
    private static RegionManager regionManager;
    private static WorldGuardPlugin worldGuardPlugin;
    private static PlayerJsonData playerJsonData;
    private static  int defaultRadius;

    public static BorderCache borderCache = new BorderCache();
    public static BalanceCache balanceCache = new BalanceCache();
    public static SelectorCache selectorCache = new SelectorCache();

    public static void init(){
        alliancePlugin = AlliancePlugin.getInstance();
        defaultRadius = alliancePlugin.radius;
        regionManager = alliancePlugin.defaultRegionManager;
        worldGuardPlugin = alliancePlugin.worldGuardPlugin;
        playerJsonData = AlliancePlugin.playerJsonData;
    }

    // Main functionalities

    public static void createDefaultRegion(Player player, Block targetBlock) {
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

        getServer().getScheduler().runTaskAsynchronously(alliancePlugin, () -> ConfigLoader.saveConfig(AlliancePlugin.playerJsonData,alliancePlugin.playerJsonFile));

        if (getRegionsInRegion(defaultRegion).size() == 0){
            regionManager.addRegion(defaultRegion);
            AllianceScoreboardManager.updateAllPlayerScores(player, EnumObjective.BALANCE);
            player.sendMessage( "Created default region at " +  targetBlock.getX() + ", " + targetBlock.getY()+ ", " + targetBlock.getZ() + " radius: " + defaultRadius);
        }else{
            player.sendMessage("You can't create a claim that overlaps with another claim");
        }
    }

    public static void expandPlayerRegion(Player player, Block targetBlock) {
        var cachedCorner = selectorCache.get(player);
        var _region = (ProtectedCuboidRegion) regionManager.getRegion(cachedCorner.regionId);
        var playerData = playerJsonData.players.get(player.getName());

        if (cachedCorner.oldCorner.equals(BlockVector2.at(targetBlock.getX(),targetBlock.getZ()))){
            player.sendMessage( "You can't select the same point to expand");
            selectorCache.reset(player);
            return;
        }

        if (_region !=null){
            var newCornerValue = getNewCorners(_region, targetBlock,cachedCorner);


            //make a copy of the region (avoid mutating existing regions)
            ProtectedCuboidRegion newRegion = new ProtectedCuboidRegion(_region.getId(),
                    newCornerValue._1.toBlockVector3(0),
                    newCornerValue._2.toBlockVector3(targetBlock.getWorld().getMaxHeight()));

            newRegion.copyFrom(_region);

            var oldArea = calculateArea(_region);
            var newArea = calculateArea(newRegion);
            var tempBalanceDifference = oldArea - newArea;
            var newBalance = playerData.balance + tempBalanceDifference;

            //calculate if new region is smaller than the allowed size

            var sides = calculateSidesLength(newRegion);

            if (sides._1 <  ((defaultRadius *2) + 1) ||
                    sides._2 <  ((defaultRadius *2) + 1) ){
                player.sendMessage("Region cannot be smaller than " + ((defaultRadius *2) + 1));
                selectorCache.reset(player);
                return;
            }

            if (newBalance < 0){
                player.sendMessage( "You don't have enough block balance to do that!");
            }

            if (getRegionsInRegion(newRegion,_region).size() == 0){
                regionManager.removeRegion(_region.getId());
                regionManager.addRegion(newRegion);
                playerData.balance = newBalance;
                AllianceScoreboardManager.updatePlayerScore(player,EnumScore.BALANCE_CURRENT);
                getServer().getScheduler().runTaskAsynchronously(alliancePlugin, () -> ConfigLoader.saveConfig(AlliancePlugin.playerJsonData,alliancePlugin.playerJsonFile));

                AlliancePlugin.printThread("expandPlayerRegion");

                var _oldBlocksHighlighted = borderCache.get(player).get(_region.getId());
                var oldHighlighted = new ArrayList<BlockVector3>();
                _oldBlocksHighlighted.forEach(block -> oldHighlighted.add(BlockVector3.at(block.getX(),block.getY(),block.getZ() )));
                var newHighlighted = getBorderBlocks(newRegion, player, (highlightLocation, blockMaterial) -> { });

                oldHighlighted.removeAll(newHighlighted);

                borderCache.reset(player, oldHighlighted)
                    .thenApply(empty -> {
                        AlliancePlugin.printThread("thenApply");
                        highlightRegionsForPlayer(player);
                        return null;
                    });

                player.sendMessage( "Region expanded to: " +  newCornerValue._1.toString() + ", " + newCornerValue._2.toString());
            }else{
                player.sendMessage("You can't modify this claim to overlap another claim");
            }

        }else{
            player.sendMessage( "The region you selected does not exist anymore");
        }

        selectorCache.reset(player);
    }

    public static void calculateNewRegionCost(Player player) {
        var targetBlock = player.getTargetBlock(5);
        var cachedCorner = selectorCache.get(player);
        if (cachedCorner == null || targetBlock == null){return;}
        var _region = (ProtectedCuboidRegion) regionManager.getRegion(cachedCorner.regionId);

        var newCornerValue = getNewCorners(_region, targetBlock,cachedCorner);

        if (cachedCorner.oldCorner.equals(BlockVector2.at(targetBlock.getX(),targetBlock.getZ()))){
            balanceCache.reset(player);
            return;
        }

        //make a copy of the region and calulate the volume (since its flat we will get an area)
        ProtectedCuboidRegion newRegion = new ProtectedCuboidRegion(_region.getId(),
                newCornerValue._1.toBlockVector3(0),
                newCornerValue._2.toBlockVector3(0));

        var oldArea = _region.volume() / (_region.getMaximumPoint().getBlockY() - _region.getMinimumPoint().getBlockY());
        var newArea = newRegion.volume();

        balanceCache.add(player, oldArea - newArea);
    }

    public static void highlightRegionsForPlayer(Player p) {
        //highlight edges of regions within a certain area
        int h_radius = 20;
        Location targetLocation = p.getLocation();

        var corners = getCornersAsBlockVector(targetLocation,h_radius,targetLocation.getBlockY()+h_radius, targetLocation.getBlockY()-h_radius);

        var targetRegion = new ProtectedCuboidRegion("temp",true, corners._1, corners._2);

        var regions = getRegionsInRegion(targetRegion);

        AlliancePlugin.printThread("highlightRegionsForPlayer");

        for (var region:regions) {
            if (!region.isOwner(alliancePlugin.worldGuardPlugin.wrapPlayer(p))){
                continue;
            }
            ArrayList<Block> tempBlocks = new ArrayList<>();
            HashMap<String,ArrayList<Block>> tempMap = new HashMap<>();
            tempMap.put(region.getId(), tempBlocks);

            getBorderBlocks((ProtectedCuboidRegion) region, p,
                (highlightLocation, blockMaterial) -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (blockMaterial != Material.GOLD_BLOCK) {
                                p.sendBlockChange(highlightLocation, Material.GOLD_BLOCK.createBlockData());
                            }
                        }
                    }.runTaskAsynchronously(alliancePlugin);

                    tempBlocks.add(p.getWorld().getBlockAt(highlightLocation));
                }
            );

            if (borderCache.get(p) != null ){
                borderCache.get(p).put(region.getId(), tempBlocks);

            }else{
                borderCache.add(p, tempMap);
            }

        }
    }

    public static boolean selectFirstCornerBlock(Player player, Block targetBlock, ArrayList<ProtectedRegion> ownedRegions) {
        var clickedRegion = ownedRegions.get(0);
        //check if block clicked is a corner block
        var cornerType = determineCorner(clickedRegion, targetBlock);

        if (cornerType != null){
            var selectorCacheValues = new Corner(clickedRegion.getId(), targetBlock,cornerType);
            selectorCache.add(player,selectorCacheValues);
            player.sendMessage( "Corner " + selectorCacheValues.cornerTypes + " selected for expansion");
            return true;
        }
        return false;
    }

    public static void promptUserToDelete(Player player, ProtectedRegion region) {
        final TextComponent textComponent = Component.text("Do you want to delete this region? Click here to delete >>>")
                .append(Component.text("Yes").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,"/claims delete "+region.getId()))
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.BOLD,true)
                )
                ;
        player.sendMessage(textComponent);

    }

    public static void promptUserToCreate(Player player, Block block){
        final TextComponent textComponent = Component.text("Do you want to create a region? Click here to create >>>")
                .append(Component.text("Yes")
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,String.format("/claims create %s %s %s", block.getX(), block.getY(),block.getZ() )))
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.BOLD,true)
                )
                ;
        player.sendMessage(textComponent);
    }

    //Helper methods
    public static ArrayList<BlockVector3> getBorderBlocks(ProtectedCuboidRegion region, Player p, CallbackForEachBlock callbackForEachBlock){
        var returnArray = new ArrayList<BlockVector3>();
        var min = region.getMinimumPoint();
        var max = region.getMaximumPoint();

        var minFlat= BlockVector3.at(min.getBlockX(),p.getLocation().getY(),  min.getBlockZ());
        var maxFlat= BlockVector3.at(max.getBlockX(),p.getLocation().getY(), max.getBlockZ());

        var cuboid = new CuboidRegion(BukkitAdapter.adapt(p.getWorld()),minFlat,maxFlat);

        var walls = cuboid.getWalls();

        for (var block:walls ) {

            var _y  = p.getWorld().getHighestBlockYAt(block.getBlockX(), block.getBlockZ());

            var highlightLocation = new Location(p.getWorld(), block.getBlockX(),_y,block.getBlockZ() );
            var blockMaterial = p.getWorld().getBlockAt(highlightLocation).getBlockData().getMaterial();
            while (blockMaterial == Material.WATER ||
                    blockMaterial == Material.LAVA) {
                _y -= 1;
                highlightLocation = new Location(p.getWorld(), block.getBlockX(),_y,block.getBlockZ() );
                blockMaterial = p.getWorld().getBlockAt(highlightLocation).getBlockData().getMaterial();
                if (_y == 0 || _y == p.getWorld().getMaxHeight()){break;}
            }

            returnArray.add(BukkitAdapter.asBlockVector(highlightLocation));

            callbackForEachBlock.block(highlightLocation,blockMaterial);

        }
        return returnArray;
    }

    public static ProtectedRegion generateDefaultRegion(Player player, LocalPlayer localPlayer, PlayerData playerData, Block targetBlock,int radius) {
        //target block is never null because of RIGHT_CLICK_BLOCK Action
        var targetLocation = targetBlock.getLocation();

        var corners = getCornersAsBlockVector(targetLocation,radius, 0, player.getWorld().getMaxHeight());

        var defaultRegion = new ProtectedCuboidRegion("region_"+ player.getName()+ "_" + playerData.regionsCreated,corners._1, corners._2);

        defaultRegion.getOwners().addPlayer(localPlayer);
        defaultRegion.setFlag(Flags.FIRE_SPREAD, DENY);
        return defaultRegion;
    }

    public static Tuple<BlockVector3, BlockVector3> getCornersAsBlockVector(Location targetLocation, int radius, int minY, int maxY){
        Location corner1Location = new Location(targetLocation.getWorld(), targetLocation.getX() + radius, maxY, targetLocation.getZ() + radius );
        Location corner2Location = new Location(targetLocation.getWorld(), targetLocation.getX() - radius,minY, targetLocation.getZ() - radius );

        BlockVector3 corner1Vector3 = BukkitAdapter.asBlockVector(corner1Location);
        BlockVector3 corner2Vector3 = BukkitAdapter.asBlockVector(corner2Location);

        return new Tuple<>(corner1Vector3, corner2Vector3);
    }

    public static CornerTypes determineCorner(ProtectedRegion region, Block selectedBlock){
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

    public static Tuple<BlockVector2, BlockVector2> getNewCorners(ProtectedCuboidRegion region, Block selectedBlock, Corner corner){
        var min =  region.getMinimumPoint().toBlockVector2();
        var max =  region.getMaximumPoint().toBlockVector2();
        var newCorner = asBlockVector2(selectedBlock);
        var oldCorner = corner.oldCorner;
        var xMin_old = min.getBlockX();
        var zMin_old = min.getBlockZ();
        var xMax_old = max.getBlockX();
        var zMax_old = max.getBlockZ();

        BlockVector2 displacement = newCorner.subtract(oldCorner);
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

    public static BlockVector2 asBlockVector2(Block block){
        return BlockVector2.at(block.getX(),block.getZ());
    }

    public static Set<ProtectedRegion> getRegionsInRegion(ProtectedRegion region){
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
    public static Set<ProtectedRegion> getRegionsInRegion(ProtectedRegion region, ProtectedRegion oldRegion){

        //oldRegionis used, in case we have an old region and still want to check for other regions
        //but excluding that oldRegion
        var regions = regionManager.getApplicableRegions(region).getRegions();
        regions.remove(region);
        regions.remove(oldRegion);

        return regions;
    }

    /**
     * Calculate sides for a region
     * @param region the region we want to calculate
     * @return HorizontalSide, VerticalSide
     */
    public static Tuple<Integer,Integer> calculateSidesLength(ProtectedCuboidRegion region){
        //calculate if new region is smaller than the allowed size
        var newMax = region.getMaximumPoint();
        var newMin = region.getMinimumPoint();

        var horizontalSide = Math.abs(newMax.getBlockX() - newMin.getBlockX());
        var verticalSide = Math.abs(newMax.getBlockZ() - newMin.getBlockZ());

        return new Tuple<>(horizontalSide + 1,verticalSide + 1);
    }

    public static int calculateArea(ProtectedCuboidRegion region){
        var sides =calculateSidesLength(region);

        return sides._1 * sides._2;
    }

}                
