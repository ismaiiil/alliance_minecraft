package com.ismaiiil.alliance;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

import static com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;
import static com.sk89q.worldguard.protection.regions.ProtectedRegion.GLOBAL_REGION;
import static java.util.stream.Collectors.toCollection;

public final class AlliancePlugin extends JavaPlugin implements Listener {
    RegionsInstance defaultWorldFactory;
    public RegionManager defaultRegionManager;
    public RegionContainer defaultRegionContainer;
    public WorldGuardPlugin worldGuardPlugin;

    public AllianceRegionManager arm;

    public int radius;
    int defaultBalance;

    private static AlliancePlugin inst;

    public PlayerJsonData playerJsonData;
    public File playerJsonFile;

    public AllianceScoreboardManager allianceScoreboardManager;
    EnumObjective[] enumObjectives;
    public final long SERVER_TICK = 20L;

    private int minPoolSize;
    private int maxPoolSize;
    public static ExecutorService pool;


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

        minPoolSize = getConfig().getInt("performance.min-threads");
        maxPoolSize = getConfig().getInt("performance.max-threads");


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
        arm = AllianceRegionManager.getInstance();

//        pool = new ThreadPoolExecutor(minPoolSize, maxPoolSize, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(maxPoolSize));
        pool = new ThreadPoolExecutor(minPoolSize, maxPoolSize, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(maxPoolSize),
                new ThreadFactoryBuilder()
                        .setNameFormat("ALLIANCE POOL-%d")
                        .setDaemon(false)
                        .build());

        //TODO optimise highlighting to not highlight already highlighted blocks
        //TODO fix highlight flicker
        //TODO implement random tp into alliance
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

            arm.borderCache.tasks.stop(p);
            arm.borderCache.reset(p);

            arm.selectorCache.reset(p);

            arm.balanceCache.tasks.stop(p);
            arm.balanceCache.reset(p);

        }
        if (newItem.getType() == EnumObjective.BALANCE.getScoreboardItem()){

            allianceScoreboardManager.setPlayerScoreboard(p);
            updatePlayerScoreboard(p,EnumObjective.BALANCE);

            arm.borderCache.tasks.stop(p);
            arm.borderCache.tasks.add(p,() -> {
                printThread("scheduleAsync");
                arm.highlightRegionsForPlayer(p);
            }, 0L, SERVER_TICK * 2 );


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

        arm.borderCache.tasks.stop(p);
        arm.selectorCache.reset(p);
        arm.balanceCache.tasks.stop(p);
        arm.balanceCache.reset(p);
        allianceScoreboardManager.deletePlayerScoreboard(p);

        //TODO remove this later
//        var regs =  defaultRegionManager.getRegions();
//        for (var reg: regs.values()) {
//            defaultRegionManager.removeRegion(reg.getId());
//        }


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

                if ( arm.selectorCache.get(player) == null){
                    if (ownedRegionsAtBlock.size() == 0){
                        arm.createDefaultRegion(player, targetBlock);
                    }else{
                        var hasSucceeded = arm.selectFirstCornerBlock(player, targetBlock, ownedRegionsAtBlock);
                        if (hasSucceeded){
                            arm.balanceCache.tasks.add(player, new BukkitRunnable() {
                                @Override
                                public void run() {
                                    arm.calculateNewRegionCost(player);
                                }
                            }, 0, SERVER_TICK/10);

                        }else{
                            arm.promptUserToDelete(player, ownedRegionsAtBlock.get(0));
                        }
                    }
                }else{

                    printThread("mainThread  ");

                    arm.expandPlayerRegion(player, targetBlock);

                    arm.balanceCache.tasks.stop(player);
                    arm.balanceCache.reset(player);
                }

            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e){
        var theCommandRan = e.getMessage();

        if (theCommandRan.contains("/rg rem")){
            arm.borderCache.reset(e.getPlayer()).thenApply(
                    bool ->{
                        arm.highlightRegionsForPlayer(e.getPlayer());
                        allianceScoreboardManager.updateAllPlayerScores(e.getPlayer(),EnumObjective.BALANCE);
                        return null;
                    });

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

    public static void printThread(String s) {
        //System.out.println("Current THREAD: " + Thread.currentThread().getName() + " running inside " + s);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ConfigLoader.saveConfig(playerJsonData,playerJsonFile);


    }


}
