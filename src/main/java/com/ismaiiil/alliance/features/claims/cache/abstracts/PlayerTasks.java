package com.ismaiiil.alliance.features.claims.cache.abstracts;

import com.ismaiiil.alliance.AlliancePlugin;
import lombok.NoArgsConstructor;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
public class PlayerTasks {
    private final ConcurrentHashMap<UUID, BukkitTask> playerToTasks = new ConcurrentHashMap<>();


    public void stop(Player p) {
        if (playerToTasks.containsKey(p.getUniqueId())) {
            playerToTasks.get(p.getUniqueId()).cancel();
            playerToTasks.remove(p.getUniqueId());
        }
    }

    public void add(Player p, Runnable bukkitRunnable, long delay, long period){
        var task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(AlliancePlugin.getInstance(),bukkitRunnable,delay,period);
        playerToTasks.put(p.getUniqueId(),task);
    }


}
