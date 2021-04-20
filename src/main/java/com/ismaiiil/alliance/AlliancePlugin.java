package com.ismaiiil.alliance;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ismaiiil.alliance.commands.ACommandManager;
import com.ismaiiil.alliance.land.AllianceRegionManager;
import com.ismaiiil.alliance.json.PlayerJsonData;
import com.ismaiiil.alliance.json.PlayerData;
import com.ismaiiil.alliance.json.ConfigLoader;
import com.ismaiiil.alliance.scoreboard.AllianceScoreboardManager;
import com.ismaiiil.alliance.scoreboard.EnumObjective;
import com.ismaiiil.alliance.scoreboard.EnumScore;
import com.ismaiiil.alliance.worldguardinstance.RegionsInstance;
import com.ismaiiil.alliance.worldguardinstance.WorldHelperFactory;
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


    public int radius;
    int defaultBalance;

    private static AlliancePlugin inst;

    public static PlayerJsonData playerJsonData;
    public File playerJsonFile;

    EnumObjective[] enumObjectives;
    public final long SERVER_TICK = 20L;

    public static ExecutorService pool;

    public AlliancePlugin(){
        inst = this;
    }

    public static AlliancePlugin getInstance() {
        return inst;
    }

    @Override
    public void onLoad() {
        ACommandManager.init();
    }

    @Override
    public void onEnable() {

        // TODO find better way loading up enums
        enumObjectives = EnumObjective.myValues();
        var enumScores = EnumScore.values();

        //config files init
        saveDefaultConfig();
        radius = getConfig().getInt("defaults.radius");
        defaultBalance = getConfig().getInt("defaults.starting-balance");

        int minPoolSize = getConfig().getInt("performance.min-threads");
        int maxPoolSize = getConfig().getInt("performance.max-threads");


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

        AllianceRegionManager.init();
        AllianceScoreboardManager.init();

        //setting default worldGuard stuff
        setGlobalFlags();

//        pool = new ThreadPoolExecutor(minPoolSize, maxPoolSize, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(maxPoolSize));
        pool = new ThreadPoolExecutor(minPoolSize, maxPoolSize, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(maxPoolSize),
                new ThreadFactoryBuilder()
                        .setNameFormat("ALLIANCE POOL-%d")
                        .setDaemon(false)
                        .build());

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!playerJsonData.players.containsKey(player.getName())){
            playerJsonData.createPlayerData(player.getName(), defaultBalance);
        }

        AllianceScoreboardManager.setPlayerScoreboard(player);
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

            AllianceRegionManager.borderCache.tasks.stop(p);
            AllianceRegionManager.borderCache.reset(p);

            AllianceRegionManager.selectorCache.reset(p);

            AllianceRegionManager.balanceCache.tasks.stop(p);
            AllianceRegionManager.balanceCache.reset(p);

        }
        if (newItem.getType() == EnumObjective.BALANCE.getScoreboardItem()){

            AllianceScoreboardManager.setPlayerScoreboard(p);
            setAndUpdatePlayerScoreboard(p,EnumObjective.BALANCE);

            AllianceRegionManager.borderCache.tasks.stop(p);
            AllianceRegionManager.borderCache.tasks.add(p,() -> {
                printThread("scheduleAsync");
                AllianceRegionManager.highlightRegionsForPlayer(p);
            }, 0L, SERVER_TICK * 2 );


        }else if(newItem.getType() == EnumObjective.WAR.getScoreboardItem()){
            AllianceScoreboardManager.setPlayerScoreboard(p);
            setAndUpdatePlayerScoreboard(p,EnumObjective.WAR);

        }else{
            AllianceScoreboardManager.resetPlayerScoreboard(p);

        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var p = event.getPlayer();

        AllianceRegionManager.borderCache.tasks.stop(p);
        AllianceRegionManager.selectorCache.reset(p);
        AllianceRegionManager.balanceCache.tasks.stop(p);
        AllianceRegionManager.balanceCache.reset(p);
        AllianceScoreboardManager.deletePlayerScoreboard(p);

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

        //check if player has a balance in balance.json else create a new entry for him
        if (!playerJsonData.players.containsKey(player.getName())){
             playerJsonData.createPlayerData(player.getName(), defaultBalance);
        }

        if ( action.equals( Action.RIGHT_CLICK_BLOCK ) ) {
            if ( item != null && item.getType() == EnumObjective.BALANCE.getScoreboardItem() ) {

                var targetBlock = event.getClickedBlock();
                //get regions at clicked block
                Set<ProtectedRegion> regionsAtBlock;
                if (targetBlock != null) {
                    regionsAtBlock = defaultRegionManager.getApplicableRegions(BukkitAdapter.asBlockVector(targetBlock.getLocation())).getRegions();
                }else {
                    return;
                }
                ArrayList<ProtectedRegion> ownedRegionsAtBlock = regionsAtBlock.stream().filter(region -> region.isOwner(localPlayer)).collect(toCollection(ArrayList::new));

                for (var region: regionsAtBlock) {
                    if (region.isOwner(localPlayer)){
                        ownedRegionsAtBlock.add(region);
                    }
                }

                if ( AllianceRegionManager.selectorCache.get(player) == null){
                    if (ownedRegionsAtBlock.size() == 0){
                        AllianceRegionManager.createDefaultRegion(player, targetBlock);
                    }else{
                        var hasSucceeded = AllianceRegionManager.selectFirstCornerBlock(player, targetBlock, ownedRegionsAtBlock);
                        if (hasSucceeded){
                            AllianceRegionManager.balanceCache.tasks.add(player,
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    AllianceRegionManager.calculateNewRegionCost(player);
                                }
                            }, 0, SERVER_TICK/10);

                        }else{
                            AllianceRegionManager.promptUserToDelete(player, ownedRegionsAtBlock.get(0));
                        }
                    }
                }else{

                    printThread("mainThread  ");

                    AllianceRegionManager.expandPlayerRegion(player, targetBlock);

                    AllianceRegionManager.balanceCache.tasks.stop(player);
                    AllianceRegionManager.balanceCache.reset(player);
                }

            }
        }
    }


    private void setAndUpdatePlayerScoreboard(Player player, EnumObjective enumObjective){
        if (enumObjective != null){
            AllianceScoreboardManager.setPlayerSidebar(player, enumObjective);
            AllianceScoreboardManager.updateAllPlayerScores(player,enumObjective);
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

    public static PlayerData getPlayerData(Player player){
        return playerJsonData.getPlayerData(player.getName());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ConfigLoader.saveConfig(playerJsonData,playerJsonFile);


    }


}
