package com.ismaiiil.alliance;

import com.ismaiiil.alliance.WorldGuardInstances.RegionsInstance;
import com.ismaiiil.alliance.WorldGuardInstances.WorldHelperFactory;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

import static com.sk89q.worldguard.protection.regions.ProtectedRegion.GLOBAL_REGION;

public final class Alliance extends JavaPlugin implements Listener {
    RegionsInstance defaultWorldFactory;
    RegionManager defaultRegionManager;
    RegionContainer defaultRegionContainer;
    @Override
    public void onEnable() {
        // Plugin startup logic

        defaultWorldFactory = WorldHelperFactory.getWorldFactory("world");

        if (defaultWorldFactory != null){
            defaultRegionManager = defaultWorldFactory.worldRegionManager;
            defaultRegionContainer = defaultWorldFactory.worldRegionContainer;
        }

        setGlobalFlags();


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

            globalRegion.setFlag(Flags.PVP, StateFlag.State.DENY);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


}
