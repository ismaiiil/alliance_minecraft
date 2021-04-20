package com.ismaiiil.alliance.land.cache.implemented;

import com.ismaiiil.alliance.land.cache.abstracts.PlayerCache;
import com.ismaiiil.alliance.land.cache.abstracts.PlayerTasks;
import com.ismaiiil.alliance.scoreboard.AllianceScoreboardManager;
import com.ismaiiil.alliance.scoreboard.EnumScore;
import org.bukkit.entity.Player;

public class BalanceCache extends PlayerCache<Integer, Boolean> {
    public PlayerTasks tasks = new PlayerTasks();

    @Override
    public Boolean reset(Player player) {
        cache.remove(player.getUniqueId());
        AllianceScoreboardManager.updatePlayerScore(player, EnumScore.BALANCE_CURRENT);
        return true;
    }

    @Override
    public void add(Player player, Integer integer) {
        super.add(player, integer);
        AllianceScoreboardManager.updatePlayerScore(player, EnumScore.BALANCE_CURRENT);
    }
}
