package com.ismaiiil.alliance.worldguardinstance;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.World;

import java.util.HashMap;

import static org.bukkit.Bukkit.getServer;

public class WorldHelperFactory {

    public static HashMap<String, RegionsInstance> regionInstancesMap= new HashMap<>();

    public static RegionsInstance getWorldFactory(String worldName){
        if (regionInstancesMap.containsKey(worldName)){
            return regionInstancesMap.get(worldName);
        }else{
            RegionContainer _rc = WorldGuard.getInstance().getPlatform().getRegionContainer();

            World world = getServer().getWorld(worldName);
            if(world == null){
                return null;
            }

            RegionManager _rm = _rc.get(BukkitAdapter.adapt(world));
            if(_rm != null){
                RegionsInstance _rgi = new RegionsInstance(_rc,_rm);
                regionInstancesMap.put(worldName, _rgi);
                return _rgi;
            }
            return null;

        }

    }


}

