package com.ismaiiil.alliance;

import com.ismaiiil.alliance.Utils.ConfigLoading.JSON.PlayerJsonData;
import com.ismaiiil.alliance.Utils.ConfigLoading.JSON.PlayerData;
import com.ismaiiil.alliance.Utils.ConfigLoading.JSON.ConfigLoader;
import com.ismaiiil.alliance.Utils.Scoreboard.AllianceScoreboardManager;
import com.ismaiiil.alliance.Utils.Scoreboard.EnumObjective;
import com.ismaiiil.alliance.Utils.Scoreboard.EnumScore;
import com.ismaiiil.alliance.WorldGuardInstances.RegionsInstance;
import com.ismaiiil.alliance.WorldGuardInstances.WorldHelperFactory;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.*;
import lombok.var;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

import static com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;
import static com.sk89q.worldguard.protection.regions.ProtectedRegion.GLOBAL_REGION;

public final class AlliancePlugin extends JavaPlugin implements Listener {
    RegionsInstance defaultWorldFactory;
    public RegionManager defaultRegionManager;
    public RegionContainer defaultRegionContainer;
    public WorldGuardPlugin worldGuardPlugin;

    int radius;
    int defaultBalance;

    private static AlliancePlugin inst;

    public PlayerJsonData playerJsonData;
    public File playerJsonFile;

    AllianceScoreboardManager allianceScoreboardManager;
    EnumObjective[] enumObjectives;


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



        //custom json file initilisation
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

        //TODO set up area highlighting when equipping stick(own claims only)
        //TODO change highlighting when clicking on edge or corner
        //TODO set up area expansion (size cannot be lower than default radius *2 plus one
        //TODO setup delete region when right clicking inside region
        //TODO reduce balance accordingly when trying to expand area(dynamic display of used balance and current balance)
        //TODO creating a claim or expanding cannot intersect another claim


        //TODO disable fire spread in global constant

        //TODO refactor area claiming
        //TODO negative expansion of and edge can go in opposite direction of square( 2, 1, 0, -1, -2), this reflects the image of the area in a mirror
        //TODO make videos to help users claim areas and expanding them
        //TODO find solution if a player places 4 land claims around one users land claim

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


        if (newItem.getType() == EnumObjective.BALANCE.getScoreboardItem()){
            allianceScoreboardManager.setPlayerScoreboard(p);
            updatePlayerScoreboard(p,EnumObjective.BALANCE);

            //highlight edges of regions within a certain area
            int h_radius = 4;
            var targetLocation = p.getLocation();

            var corner1Location = new Location(p.getWorld(), targetLocation.getX() + h_radius, targetLocation.getY() + h_radius, targetLocation.getZ() + h_radius );
            var corner2Location = new Location(p.getWorld(), targetLocation.getX() - h_radius,targetLocation.getY() - h_radius , targetLocation.getZ() - h_radius );

            var corner1Vector3 = BukkitAdapter.asBlockVector(corner1Location);
            var corner2Vector3 = BukkitAdapter.asBlockVector(corner2Location);

            var targetRegion = new ProtectedCuboidRegion("temp",true, corner1Vector3, corner2Vector3);

            var regions = defaultRegionManager.getApplicableRegions(targetRegion);

            for (var region:regions) {
                System.out.println(region.getId());
                var min = region.getMinimumPoint();
                var max = region.getMaximumPoint();

                var minFlat= BlockVector2.at(min.getBlockX(), min.getBlockZ());
                var maxFlat= BlockVector2.at(max.getBlockX(), max.getBlockZ());

            }

            //TODO look into CuboidRegion#getFaces




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

                //check user balance before creating region
                int claimCost = ((radius*2) + 1) * ((radius*2) + 1);

                if(playerData.balance - claimCost < 0){
                    player.sendMessage("You dnt have enough block balance to claim this area (" + ((radius*2) + 1) + "*"+ ((radius*2) + 1) + "blocks)");
                    return;
                }

                //target block is never null because of RIGHT_CLICK_BLOCK Action
                var targetLocation = targetBlock.getLocation();

                var corner1Location = new Location(targetBlock.getWorld(), targetLocation.getX() + radius, targetLocation.getWorld().getMaxHeight(), targetLocation.getZ() + radius );
                var corner2Location = new Location(targetBlock.getWorld(), targetLocation.getX() - radius,0 , targetLocation.getZ() - radius );

                var corner1Vector3 = BukkitAdapter.asBlockVector(corner1Location);
                var corner2Vector3 = BukkitAdapter.asBlockVector(corner2Location);

                //TODO remove transient
                var defaultRegion = new ProtectedCuboidRegion("region_"+player.getName()+ "_" + playerData.regionsCreated,true, corner1Vector3, corner2Vector3);


                playerData.regionsCreated += 1;
                playerData.balance -= claimCost;
                playerData.usedBalance += claimCost;

                ConfigLoader.saveConfig(playerJsonData,playerJsonFile);

                defaultRegion.getOwners().addPlayer(localPlayer);
                defaultRegion.setFlag(Flags.USE, DENY);
                defaultRegion.setFlag(Flags.ENTRY, DENY);
                defaultRegionManager.addRegion(defaultRegion);

                allianceScoreboardManager.updateAllPlayerScores(player,EnumObjective.BALANCE);

                //
                getServer().getScheduler().runTaskAsynchronously(this, bukkitTask -> {

                });
                //defaultRegion.getIntersectingRegions()
//                player.sendBlockChange(corner1Location,Material.GOLD_BLOCK.createBlockData());
//                player.sendBlockChange(corner2Location,Material.GOLD_BLOCK.createBlockData());

                //restore with  block.getState().update();

                player.sendMessage( "success" );



            }
        }

    }

    private void updatePlayerScoreboard(Player player, EnumObjective enumObjective){
        var playerData = playerJsonData.getPlayerData(player.getName());
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
