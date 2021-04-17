package com.ismaiiil.alliance.LandClaiming.CacheTasks.Implemented;

import com.ismaiiil.alliance.LandClaiming.CacheTasks.Abstract.PlayerCache;
import com.ismaiiil.alliance.LandClaiming.CacheTasks.Abstract.PlayerTasks;
import com.ismaiiil.alliance.Scoreboard.AllianceScoreboardManager;
import com.ismaiiil.alliance.Scoreboard.EnumScore;
import org.bukkit.entity.Player;

public class BalanceCache extends PlayerCache<Integer, Boolean> {
    public PlayerTasks tasks = new PlayerTasks();

    @Override
    public Boolean reset(Player player) {
        cache.remove(player.getUniqueId());
        AllianceScoreboardManager.getInstance().updatePlayerScore(player, EnumScore.BALANCE_CURRENT);
        return true;
    }

    @Override
    public void add(Player player, Integer integer) {
        super.add(player, integer);
        AllianceScoreboardManager.getInstance().updatePlayerScore(player, EnumScore.BALANCE_CURRENT);
    }
}
