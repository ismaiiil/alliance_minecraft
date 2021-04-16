package com.ismaiiil.alliance.LandClaiming.CacheTasks.Implemented;

import com.ismaiiil.alliance.LandClaiming.CacheTasks.Abstract.PlayerCache;
import com.ismaiiil.alliance.LandClaiming.Corner;
import org.bukkit.entity.Player;

public class SelectorCache extends PlayerCache<Corner> {
    @Override
    public void reset(Player player) {
        cache.remove(player.getUniqueId());
    }
}
