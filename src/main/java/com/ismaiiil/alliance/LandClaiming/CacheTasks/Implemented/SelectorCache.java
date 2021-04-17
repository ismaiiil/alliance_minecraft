package com.ismaiiil.alliance.LandClaiming.CacheTasks.Implemented;

import com.ismaiiil.alliance.LandClaiming.CacheTasks.Abstract.PlayerCache;
import com.ismaiiil.alliance.LandClaiming.Corner;
import org.bukkit.entity.Player;

public class SelectorCache extends PlayerCache<Corner, Boolean> {
    @Override
    public Boolean reset(Player player) {
        cache.remove(player.getUniqueId());
        return true;
    }
}
