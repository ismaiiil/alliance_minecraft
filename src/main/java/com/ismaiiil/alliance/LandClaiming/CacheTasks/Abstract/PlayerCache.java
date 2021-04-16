package com.ismaiiil.alliance.LandClaiming.CacheTasks.Abstract;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PlayerCache<VALUE>{

    protected ConcurrentHashMap<UUID,VALUE> cache = new ConcurrentHashMap<>();

    public abstract void reset(Player player);

    public VALUE get(Player player){
        if (cache.containsKey(player.getUniqueId())){
            return cache.get(player.getUniqueId());
        }
        return null;
    }

    public void add(Player player, VALUE value){
        cache.put(player.getUniqueId(), value);
    }


}