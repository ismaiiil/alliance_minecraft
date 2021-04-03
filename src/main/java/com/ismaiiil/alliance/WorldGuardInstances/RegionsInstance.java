package com.ismaiiil.alliance.WorldGuardInstances;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class RegionsInstance {
    public RegionsInstance(RegionContainer worldRegionContainer, RegionManager worldRegionManager) {
        this.worldRegionContainer = worldRegionContainer;
        this.worldRegionManager = worldRegionManager;
    }

    public RegionContainer worldRegionContainer;
    public RegionManager worldRegionManager;

}
