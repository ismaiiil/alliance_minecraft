package com.ismaiiil.alliance.features.claims.cache.implemented;

import com.ismaiiil.alliance.features.claims.cache.abstracts.PlayerCache;
import com.ismaiiil.alliance.features.claims.corner.Corner;
import org.bukkit.entity.Player;

public class SelectorCache extends PlayerCache<Corner, Boolean> {
    @Override
    public Boolean reset(Player player) {
        cache.remove(player.getUniqueId());
        return true;
    }
}
