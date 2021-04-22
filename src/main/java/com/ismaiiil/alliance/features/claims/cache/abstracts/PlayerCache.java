package com.ismaiiil.alliance.features.claims.cache.abstracts;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PlayerCache<VALUE,RETURN>{

    protected ConcurrentHashMap<UUID,VALUE> cache = new ConcurrentHashMap<>();

    public abstract RETURN reset(Player player);

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
