package com.ismaiiil.alliance.LandClaiming;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.JSON.ConfigLoader;
import com.ismaiiil.alliance.JSON.PlayerData;
import com.ismaiiil.alliance.Utils.Tuple;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.var;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static com.ismaiiil.alliance.LandClaiming.CornerTypes.*;
import static com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;

public class AllianceRegionManager {
    private static AllianceRegionManager inst;
    private AlliancePlugin alliancePlugin;
    private RegionManager regionManager;

    public HashMap<UUID, Multimap<String, Block>> borderRegionCache = new HashMap<>();
    public HashMap<UUID,Integer> highlighterTasks = new HashMap<>();
    public HashMap<UUID, Corner> selectorCache = new HashMap<>();

    private AllianceRegionManager(){
        alliancePlugin = AlliancePlugin.getInstance();
        regionManager = alliancePlugin.defaultRegionManager;
    }

    public static AllianceRegionManager getInstance(){
        if(inst == null){
            inst = new AllianceRegionManager();
        }
        return inst;
    }

    public void  highlightRegionsForPlayer(Player p) {
        //highlight edges of regions within a certain area
        int h_radius = 6;
        Location targetLocation = p.getLocation();

        var corners = getCornersAsBlockVector(targetLocation,h_radius,targetLocation.getBlockY()+h_radius, targetLocation.getBlockY()-h_radius);

        var targetRegion = new ProtectedCuboidRegion("temp",true, corners._1, corners._2);

        var regions = getRegionsInRegion(targetRegion);

        for (var region:regions) {
            if (!region.isOwner(alliancePlugin.worldGuardPlugin.wrapPlayer(p))){
                return;
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
                p.sendBlockChange(highlightLocation, Material.GOLD_BLOCK.createBlockData());
                tempBlocks.put(region.getId() , p.getWorld().getBlockAt(highlightLocation));

            }

            if (borderRegionCache.containsKey(p.getUniqueId())){
                Multimap<String, Block> tempCache = borderRegionCache.get(p.getUniqueId());
                tempCache.putAll(tempBlocks);
            }else{
                borderRegionCache.put(p.getUniqueId(),tempBlocks);
            }

        }
    }

    public ProtectedRegion generateDefaultRegion(Player player, LocalPlayer localPlayer, PlayerData playerData, Block targetBlock,int radius, int claimCost) {
        //target block is never null because of RIGHT_CLICK_BLOCK Action
        var targetLocation = targetBlock.getLocation();

        var corners = getCornersAsBlockVector(targetLocation,radius, 0, player.getWorld().getMaxHeight());

        //TODO remove transient
        var defaultRegion = new ProtectedCuboidRegion("region_"+ player.getName()+ "_" + playerData.regionsCreated,corners._1, corners._2);


        playerData.regionsCreated += 1;
        playerData.balance -= claimCost;
        playerData.usedBalance += claimCost;

        ConfigLoader.saveConfig(alliancePlugin.playerJsonData,alliancePlugin.playerJsonFile);

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
        if (highlighterTasks.containsKey(p.getUniqueId())) {
            AlliancePlugin.getInstance().getServer().getScheduler().cancelTask(highlighterTasks.get(p.getUniqueId()));
            highlighterTasks.remove(p.getUniqueId());
        }
    }

    public void resetHighlightedBlocks(Player p) {
        if (borderRegionCache.containsKey(p.getUniqueId())){
            var tempBlocks = borderRegionCache.get(p.getUniqueId());
            for (var hashBlocks: tempBlocks.values()) {
                hashBlocks.getState().update();
            }
            borderRegionCache.remove(p.getUniqueId());
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

    public Set<ProtectedRegion> getRegionsInRegion(ProtectedRegion region, ProtectedRegion oldRegion){
        var regions = regionManager.getApplicableRegions(region).getRegions();
        regions.remove(region);
        regions.remove(oldRegion);

        return regions;
    }




}
