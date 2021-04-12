package com.ismaiiil.alliance;

import com.ismaiiil.alliance.LandClaiming.AllianceRegionManager;
import com.ismaiiil.alliance.LandClaiming.Corner;
import com.ismaiiil.alliance.JSON.PlayerJsonData;
import com.ismaiiil.alliance.JSON.PlayerData;
import com.ismaiiil.alliance.JSON.ConfigLoader;
import com.ismaiiil.alliance.Scoreboard.AllianceScoreboardManager;
import com.ismaiiil.alliance.Scoreboard.EnumObjective;
import com.ismaiiil.alliance.Scoreboard.EnumScore;
import com.ismaiiil.alliance.WorldGuardInstances.RegionsInstance;
import com.ismaiiil.alliance.WorldGuardInstances.WorldHelperFactory;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.*;
import lombok.var;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

import static com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;
import static com.sk89q.worldguard.protection.regions.ProtectedRegion.GLOBAL_REGION;

public final class AlliancePlugin extends JavaPlugin implements Listener {
    RegionsInstance defaultWorldFactory;
    public RegionManager defaultRegionManager;
    public RegionContainer defaultRegionContainer;
    public WorldGuardPlugin worldGuardPlugin;

    public AllianceRegionManager allianceRegionManager;

    int radius;
    int defaultBalance;

    private static AlliancePlugin inst;

    public PlayerJsonData playerJsonData;
    public File playerJsonFile;

    AllianceScoreboardManager allianceScoreboardManager;
    EnumObjective[] enumObjectives;
    public final long SERVER_TICK = 20L;


    public AlliancePlugin(){
        inst = this;
    }

    public static AlliancePlugin getInstance() {
        return inst;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic


        // TODO find better way loading up enums
        enumObjectives = EnumObjective.myValues();
        var enumScores = EnumScore.values();

        //config files init
        saveDefaultConfig();
        radius = getConfig().getInt("defaults.radius");
        defaultBalance = getConfig().getInt("defaults.starting-balance");

        for (EnumObjective _eo: enumObjectives) {

            String itemPath = "objectives-data." + _eo.toString().toLowerCase() + ".item";
            var _itemString = getConfig().getString(itemPath);

            if (_itemString != null){
                _eo.setScoreboardItem(Material.getMaterial(_itemString));
            }

        }



        //custom json file initialisation
        playerJsonFile = new File(this.getDataFolder().getPath() + File.separator + "player_data.json");
        playerJsonData = ConfigLoader.loadConfig(PlayerJsonData.class, playerJsonFile);

        //registering classes
        getServer().getPluginManager().registerEvents(this, this);

        //getting worldGuard plugin stuff
        defaultWorldFactory = WorldHelperFactory.getWorldFactory("world");
        worldGuardPlugin = WorldGuardPlugin.inst();
        if (defaultWorldFactory != null){
            defaultRegionManager = defaultWorldFactory.worldRegionManager;
            defaultRegionContainer = defaultWorldFactory.worldRegionContainer;
        }

        //setting default worldGuard stuff
        setGlobalFlags();

        //creating scoreboard
        allianceScoreboardManager = AllianceScoreboardManager.getInstance();

        //periodically create region (do not register it) and chek regions that are his and highlight them
        allianceRegionManager = AllianceRegionManager.getInstance();

        //TODO set up area expansion (size cannot be lower than default radius *2 plus one
        //TODO setup delete region when right clicking inside region
        //TODO reduce balance accordingly when trying to expand area(dynamic display current balance + or - to be used balance)
        //TODO optimise event loops


    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!playerJsonData.players.containsKey(player.getName())){
            playerJsonData.createPlayerData(player.getName(), defaultBalance);
        }

        allianceScoreboardManager.setPlayerScoreboard(player);
    }



    @EventHandler
    public void onPlayerHeld(PlayerItemHeldEvent event) {
        Player p = event.getPlayer();
        var newSlot = event.getNewSlot();
        var newItem = p.getInventory().getItem(newSlot);


        if (newItem == null){
            newItem = new ItemStack(Material.AIR);
        }

        if(newItem.getType() != EnumObjective.BALANCE.getScoreboardItem()){

            allianceRegionManager.stopPlayerHighlighterTasks(p);
            allianceRegionManager.resetHighlightedBlocks(p);
            allianceRegionManager.resetSelectorCache(p);

        }
        if (newItem.getType() == EnumObjective.BALANCE.getScoreboardItem()){

//TODO remove this later
//        var regs =  defaultRegionManager.getRegions();
//        for (var reg: regs.values()) {
//            defaultRegionManager.removeRegion(reg.getId());
//        }

            allianceScoreboardManager.setPlayerScoreboard(p);
            updatePlayerScoreboard(p,EnumObjective.BALANCE);

            var task = getServer().getScheduler().scheduleAsyncRepeatingTask(
                    this, () -> allianceRegionManager.highlightRegionsForPlayer(p),
                    0L, SERVER_TICK * 2 );

            allianceRegionManager.stopPlayerHighlighterTasks(p);
            allianceRegionManager.highlighterTasks.put(p.getUniqueId(), task);


        }else if(newItem.getType() == EnumObjective.WAR.getScoreboardItem()){
            allianceScoreboardManager.setPlayerScoreboard(p);
            updatePlayerScoreboard(p,EnumObjective.WAR);

        }else{
            allianceScoreboardManager.resetPlayerScoreboard(p);

        }
    }



    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        allianceScoreboardManager.deletePlayerScoreboard(event.getPlayer());

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var localPlayer = worldGuardPlugin.wrapPlayer(player);
        var action = event.getAction();
        var item = event.getItem();

        PlayerData playerData;
        //check if player has a balance in balance.json else create a new entry for him
        if (!playerJsonData.players.containsKey(player.getName())){
            playerData = playerJsonData.createPlayerData(player.getName(), defaultBalance);
        }else{
            playerData = playerJsonData.players.get(player.getName());
        }

        if ( action.equals( Action.RIGHT_CLICK_BLOCK ) ) {
            if ( item != null && item.getType() == EnumObjective.BALANCE.getScoreboardItem() ) {

                var targetBlock = event.getClickedBlock();

                //get regions at clicked block
                var regionsAtBlock = defaultRegionManager.getApplicableRegions(BukkitAdapter.asBlockVector(targetBlock.getLocation())).getRegions();

                ArrayList<ProtectedRegion> ownedRegions = new ArrayList<>();
                for (var region: regionsAtBlock) {
                    if (region.isOwner(localPlayer)){
                        ownedRegions.add(region);
                    }
                }
                if ( allianceRegionManager.selectorCache.containsKey(player.getUniqueId())){
                    var cachedCorner = allianceRegionManager.selectorCache.get(player.getUniqueId());
                    var _region = (ProtectedCuboidRegion) defaultRegionManager.getRegion(cachedCorner.regionId);

                    if (_region !=null){
                        var newCornerValue = allianceRegionManager.getNewCorners(_region,targetBlock,cachedCorner);


                        //make a copy of the region (avoid mutating existing regions)
                        ProtectedCuboidRegion newRegion = new ProtectedCuboidRegion(_region.getId(),
                                                                                    newCornerValue._1.toBlockVector3(0),
                                                                                    newCornerValue._2.toBlockVector3(targetBlock.getWorld().getMaxHeight()));
                        newRegion.copyFrom(_region);

                        //calculate if new region is smaller than the allowed size
                        var newMax = newRegion.getMaximumPoint();
                        var newMin = newRegion.getMinimumPoint();

                        var horizontalSide = Math.abs(newMax.getBlockX() - newMin.getBlockX());
                        var verticalSide = Math.abs(newMax.getBlockZ() - newMin.getBlockZ());

                        if (horizontalSide <  ((radius*2) + 1) ||
                            verticalSide <  ((radius*2) + 1) ){
                            player.sendMessage("Region cannot be smaller than " + ((radius*2) + 1));
                            allianceRegionManager.resetSelectorCache(player);
                            return;
                        }

                        if (allianceRegionManager.getRegionsInRegion(newRegion,_region).size() == 0){
                            defaultRegionManager.removeRegion(_region.getId());
                            defaultRegionManager.addRegion(newRegion);
                            allianceRegionManager.resetHighlightedBlocks(player);
                            getServer().getScheduler().runTaskAsynchronously(
                                    this, () -> allianceRegionManager.highlightRegionsForPlayer(player));
                            player.sendMessage( "Region expanded to: " +  newCornerValue._1.toString() + ", " + newCornerValue._2.toString());
                        }else{
                            player.sendMessage("You can't modify this claim to overlap another claim");
                        }

                    }else{
                        player.sendMessage( "The region you selected does not exist anymore");
                    }

                    allianceRegionManager.resetSelectorCache(player);
                    return;
                }

                if (ownedRegions.size() == 0){

                    //check user balance before creating region
                    int claimCost = ((radius*2) + 1) * ((radius*2) + 1);
                    if(playerData.balance - claimCost < 0){
                        player.sendMessage("You dnt have enough block balance to claim this area (" + ((radius*2) + 1) + "*"+ ((radius*2) + 1) + "blocks)");
                        return;
                    }

                    var defaultRegion = allianceRegionManager.generateDefaultRegion(player, localPlayer, playerData, targetBlock,radius, claimCost);

                    if (allianceRegionManager.getRegionsInRegion(defaultRegion).size() == 0){
                        defaultRegionManager.addRegion(defaultRegion);
                        allianceScoreboardManager.updateAllPlayerScores(player,EnumObjective.BALANCE);
                        player.sendMessage( "Created default region at " +  targetBlock.getX() + ", " + targetBlock.getY()+ ", " + targetBlock.getZ() + " radius: " + radius);
                    }else{
                        player.sendMessage("You can't create a claim that overlaps with another claim");
                    }


                }else{

                    var clickedRegion = ownedRegions.get(0);
                    //check if block clicked is a corner block
                    var cornerType = allianceRegionManager.determineCorner(clickedRegion, targetBlock);

                    if (cornerType != null){
                        var selectorCacheValues = new Corner(clickedRegion.getId(),targetBlock ,cornerType);
                        allianceRegionManager.selectorCache.put(player.getUniqueId(),selectorCacheValues);
                        player.sendMessage( "Corner " + selectorCacheValues.cornerTypes + " selected for expansion");
                    }

                }
            }
        }

    }



    private void updatePlayerScoreboard(Player player, EnumObjective enumObjective){
        if (enumObjective != null){
            allianceScoreboardManager.setPlayerSidebar(player, enumObjective);
            allianceScoreboardManager.updateAllPlayerScores(player,enumObjective);
        }

    }


    private void setGlobalFlags(){
        if(defaultWorldFactory != null){
            ProtectedRegion globalRegion;
            if (defaultRegionManager.hasRegion(GLOBAL_REGION)) {
                globalRegion = Objects.requireNonNull(defaultRegionManager.getRegion(GLOBAL_REGION));
            } else {
                globalRegion = new GlobalProtectedRegion(GLOBAL_REGION);
                defaultRegionManager.addRegion(globalRegion);
            }

            globalRegion.setFlag(Flags.PVP, DENY);
            globalRegion.setFlag(Flags.FIRE_SPREAD, DENY);
        }
    }




    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ConfigLoader.saveConfig(playerJsonData,playerJsonFile);
    }


}
