package com.ismaiiil.alliance.LandClaiming.CacheTasks.Implemented;

import com.google.common.collect.Multimap;
import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.LandClaiming.CacheTasks.Abstract.PlayerCache;
import com.ismaiiil.alliance.LandClaiming.CacheTasks.Abstract.PlayerTasks;
import lombok.var;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.Bukkit.getServer;

public class BorderCache extends PlayerCache<Multimap<String, Block>> {
    public PlayerTasks tasks = new PlayerTasks();

    @Override
    public void reset(Player player) {
        var uuid = player.getUniqueId();
        if (cache.containsKey(uuid)){
            var tempBlocks = cache.get(uuid);
            for ( var block : tempBlocks.values()){
                block.getState().update();
            }
            cache.remove(uuid);
        }

    }
}
