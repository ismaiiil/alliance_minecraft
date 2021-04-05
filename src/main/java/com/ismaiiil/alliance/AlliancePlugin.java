package com.ismaiiil.alliance;

import com.ismaiiil.alliance.Utils.BalanceWrappers.PlayerJsonData;
import com.ismaiiil.alliance.Utils.BalanceWrappers.PlayerData;
import com.ismaiiil.alliance.Utils.ConfigLoader;
import com.ismaiiil.alliance.WorldGuardInstances.RegionsInstance;
import com.ismaiiil.alliance.WorldGuardInstances.WorldHelperFactory;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

import static com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;
import static com.sk89q.worldguard.protection.regions.ProtectedRegion.GLOBAL_REGION;

public final class AlliancePlugin extends JavaPlugin implements Listener {
    RegionsInstance defaultWorldFactory;
    RegionManager defaultRegionManager;
    RegionContainer defaultRegionContainer;
    WorldGuardPlugin worldGuardPlugin;
    int radius;
    int defaultBalance;

    private static AlliancePlugin inst;

    public PlayerJsonData playerJsonData;
    public File playerJsonFile;





    public AlliancePlugin(){
        inst = this;
    }

    public static AlliancePlugin inst() {
        return inst;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        //config files init
        saveDefaultConfig();
        radius = getConfig().getInt("defaults.radius");
        defaultBalance = getConfig().getInt("defaults.starting-balance");

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
        //DECIDE WHAT TO DO ASYNC???




    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        //get all players in config file

    }

    @EventHandler
    public void onPlayerClicks(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var action = event.getAction();
        var item = event.getItem();

        PlayerData playerData;
        //check if player has a balance in balance.json else create a new entry for him
        if (!playerJsonData.players.containsKey(player.getName())){
            playerData = playerJsonData.createPlayerBalance(player.getName(), defaultBalance);
        }else{
            playerData = playerJsonData.players.get(player.getName());
        }

        if ( action.equals( Action.RIGHT_CLICK_BLOCK ) ) {
            if ( item != null && item.getType() == Material.STICK ) {

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

                var defaultRegion = new ProtectedCuboidRegion("region_"+player.getName()+ "_" + playerData.regionCount,true, corner1Vector3, corner2Vector3);

                getServer().getScheduler().runTaskAsynchronously(this, bukkitTask -> {

                });
                //defaultRegion.getIntersectingRegions()

                playerData.regionCount += 1;
                playerData.balance -= claimCost;

                ConfigLoader.saveConfig(playerJsonData,playerJsonFile);

                defaultRegion.getOwners().addPlayer(worldGuardPlugin.wrapPlayer(player));
                defaultRegion.setFlag(Flags.USE, DENY);
                defaultRegion.setFlag(Flags.ENTRY, DENY);
                defaultRegionManager.addRegion(defaultRegion);

//                player.sendBlockChange(corner1Location,Material.GOLD_BLOCK.createBlockData());
//                player.sendBlockChange(corner2Location,Material.GOLD_BLOCK.createBlockData());

                //restore with  block.getState().update();

                player.sendMessage( "success" );


            }
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
    }


}
