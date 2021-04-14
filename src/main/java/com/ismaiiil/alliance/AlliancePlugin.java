package com.ismaiiil.alliance;

import com.ismaiiil.alliance.LandClaiming.AllianceRegionManager;
import com.ismaiiil.alliance.JSON.PlayerJsonData;
import com.ismaiiil.alliance.JSON.PlayerData;
import com.ismaiiil.alliance.JSON.ConfigLoader;
import com.ismaiiil.alliance.Scoreboard.AllianceScoreboardManager;
import com.ismaiiil.alliance.Scoreboard.EnumObjective;
import com.ismaiiil.alliance.Scoreboard.EnumScore;
import com.ismaiiil.alliance.WorldGuardInstances.RegionsInstance;
import com.ismaiiil.alliance.WorldGuardInstances.WorldHelperFactory;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.*;
import lombok.var;
import org.bukkit.Bukkit;
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
import static java.util.stream.Collectors.toCollection;

public final class AlliancePlugin extends JavaPlugin implements Listener {
    RegionsInstance defaultWorldFactory;
    public RegionManager defaultRegionManager;
    public RegionContainer defaultRegionContainer;
    public WorldGuardPlugin worldGuardPlugin;

    public AllianceRegionManager allianceRegionManager;

    public int radius;
    int defaultBalance;

    private static AlliancePlugin inst;

    public PlayerJsonData playerJsonData;
    public File playerJsonFile;

    public AllianceScoreboardManager allianceScoreboardManager;
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


        //TODO setup delete region when right clicking inside region (rafine zafer la)
        //TODO fix null pointer (AllianceScoreboardManager.java:99)
        //TODO fix random tp to only tp players outside of regions
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

            allianceRegionManager.stopBalanceChangeTasks(p);
            allianceRegionManager.resetBalanceChange(p);

        }
        if (newItem.getType() == EnumObjective.BALANCE.getScoreboardItem()){

            allianceScoreboardManager.setPlayerScoreboard(p);
            updatePlayerScoreboard(p,EnumObjective.BALANCE);

            var task = getServer().getScheduler().scheduleAsyncRepeatingTask(
                    this, () -> allianceRegionManager.highlightRegionsForPlayer(p),
                    0L, SERVER_TICK * 2 );

            allianceRegionManager.stopPlayerHighlighterTasks(p);
            allianceRegionManager.borderHighlighterTasks.put(p.getUniqueId(), task);


        }else if(newItem.getType() == EnumObjective.WAR.getScoreboardItem()){
            allianceScoreboardManager.setPlayerScoreboard(p);
            updatePlayerScoreboard(p,EnumObjective.WAR);

        }else{
            allianceScoreboardManager.resetPlayerScoreboard(p);

        }
    }



    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var p = event.getPlayer();
        allianceScoreboardManager.deletePlayerScoreboard(p);

        allianceRegionManager.stopPlayerHighlighterTasks(p);

        allianceRegionManager.resetSelectorCache(p);

        allianceRegionManager.stopBalanceChangeTasks(p);
        allianceRegionManager.resetBalanceChange(p);

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
             playerJsonData.createPlayerData(player.getName(), defaultBalance);
        }

        if ( action.equals( Action.RIGHT_CLICK_BLOCK ) ) {
            if ( item != null && item.getType() == EnumObjective.BALANCE.getScoreboardItem() ) {

                var targetBlock = event.getClickedBlock();

                //get regions at clicked block
                var regionsAtBlock = defaultRegionManager.getApplicableRegions(BukkitAdapter.asBlockVector(targetBlock.getLocation())).getRegions();
                ArrayList<ProtectedRegion> ownedRegionsAtBlock = regionsAtBlock.stream().filter(region -> region.isOwner(localPlayer)).collect(toCollection(ArrayList::new));

                for (var region: regionsAtBlock) {
                    if (region.isOwner(localPlayer)){
                        ownedRegionsAtBlock.add(region);
                    }
                }

                if ( !allianceRegionManager.selectorCache.containsKey(player.getUniqueId())){
                    if (ownedRegionsAtBlock.size() == 0){
                        allianceRegionManager.createDefaultRegion(player, targetBlock);
                    }else{
                        var hasSucceeded = allianceRegionManager.selectFirstCornerBlock(player, targetBlock, ownedRegionsAtBlock);
                        if (hasSucceeded){
                            var task = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
                                allianceRegionManager.calculateNewRegionCost(player);
                            },0, SERVER_TICK/10);
                            allianceRegionManager.balanceChangeTasks.put(player.getUniqueId(),task);
                        }else{
                            allianceRegionManager.promptUserToDelete(player, ownedRegionsAtBlock.get(0));
                        }

                    }
                }else{
                    allianceRegionManager.expandPlayerRegion(player, targetBlock);

                    allianceRegionManager.stopBalanceChangeTasks(player);
                    allianceRegionManager.resetBalanceChange(player);

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

        //TODO remove this later
//        var regs =  defaultRegionManager.getRegions();
//        for (var reg: regs.values()) {
//            defaultRegionManager.removeRegion(reg.getId());
//        }
    }


}
