package com.ismaiiil.alliance;

import com.ismaiiil.alliance.Utils.Balance.PlayerJsonData;
import com.ismaiiil.alliance.Utils.Balance.PlayerData;
import com.ismaiiil.alliance.Utils.ConfigLoader;
import com.ismaiiil.alliance.WorldGuardInstances.RegionsInstance;
import com.ismaiiil.alliance.WorldGuardInstances.WorldHelperFactory;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
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

    public PlayerJsonData playerJsonData;
    public File playerJsonFile;

    private static AlliancePlugin inst;

    public AlliancePlugin(){
        inst = this;
    }

    public static AlliancePlugin inst() {
        return inst;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        saveDefaultConfig();
        radius = getConfig().getInt("defaults.radius");
        defaultBalance = getConfig().getInt("defaults.starting-balance");

        playerJsonFile = new File(this.getDataFolder().getPath() + File.separator + "player_data.json");
        playerJsonData = ConfigLoader.loadConfig(PlayerJsonData.class, playerJsonFile);

        getServer().getPluginManager().registerEvents(this, this);

        defaultWorldFactory = WorldHelperFactory.getWorldFactory("world");
        worldGuardPlugin = WorldGuardPlugin.inst();
        if (defaultWorldFactory != null){
            defaultRegionManager = defaultWorldFactory.worldRegionManager;
            defaultRegionContainer = defaultWorldFactory.worldRegionContainer;
        }

        setGlobalFlags();

//        balance.balances.put("player1", new BalanceElements(242));
//        ConfigLoader.saveConfig(balance, balancesFile);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        //get all players in config file

    }

    @EventHandler
    public void onPlayerClicks(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();

        PlayerData playerData;
        //check if player has a balance in balance.json else create a new entry for him
        if (!playerJsonData.players.containsKey(player.getName())){
            playerData = playerJsonData.createPlayerBalance(player.getName(), defaultBalance);
        }else{
            playerData = playerJsonData.players.get(player.getName());
        }

        if ( action.equals( Action.RIGHT_CLICK_BLOCK ) ) {
            if ( item != null && item.getType() == Material.STICK ) {
                Block targetBlock = event.getClickedBlock();

                Location targetLocation = targetBlock.getLocation();

                Location corner1Location = new Location(targetBlock.getWorld(), targetLocation.getX() + radius, targetLocation.getWorld().getMaxHeight(), targetLocation.getZ() + radius );
                Location corner2Location = new Location(targetBlock.getWorld(), targetLocation.getX() - radius,0 , targetLocation.getZ() - radius );

                BlockVector3 corner1Vector3 = BukkitAdapter.asBlockVector(corner1Location);
                BlockVector3 corner2Vector3 = BukkitAdapter.asBlockVector(corner2Location);

                ProtectedRegion defaultRegion = new ProtectedCuboidRegion("region_"+player.getName()+ "_" + playerData.regionCount,true, corner1Vector3, corner2Vector3);

                getServer().getScheduler().runTaskAsynchronously(this, bukkitTask -> {

                });
                //defaultRegion.getIntersectingRegions()

                playerData.regionCount += 1;
                playerData.balance -= ((radius*2) + 1) * ((radius*2) + 1);

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
