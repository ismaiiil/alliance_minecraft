package com.ismaiiil.alliance.land.cache.implemented;

import com.ismaiiil.alliance.land.cache.abstracts.PlayerCache;
import com.ismaiiil.alliance.land.corner.Corner;
import org.bukkit.entity.Player;

public class SelectorCache extends PlayerCache<Corner, Boolean> {
    @Override
    public Boolean reset(Player player) {
        cache.remove(player.getUniqueId());
        return true;
    }
}
