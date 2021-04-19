package com.ismaiiil.alliance.LandClaiming.CacheTasks.Implemented;

import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.LandClaiming.CacheTasks.Abstract.PlayerCache;
import com.ismaiiil.alliance.LandClaiming.CacheTasks.Abstract.PlayerTasks;
import com.sk89q.worldedit.math.BlockVector3;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class BorderCache extends PlayerCache<HashMap<String, ArrayList <Block>> , CompletableFuture<Void>> {
    public PlayerTasks tasks = new PlayerTasks();

    @Override
    public CompletableFuture<Void> reset(Player player) {

        var uuid = player.getUniqueId();
        if (cache.containsKey(uuid)) {
            var tempList = cache.get(uuid);

            return CompletableFuture.supplyAsync(() -> {
                for (var list : tempList.values()){ for (var block: list){ block.getState().update();} }
                cache.remove(uuid);
                return null;
            }, AlliancePlugin.pool);
        }
        return null;
    }

    //resets only the blocks specified in the blocksToReset variable (useful when expanding region, we dnt want to reset
    //already highlighted blocks
    public CompletableFuture<Void> reset(Player player, ArrayList<BlockVector3> blocksToReset) {
        AlliancePlugin.printThread("start of reset");
        var uuid = player.getUniqueId();
        if (cache.containsKey(uuid)) {
            var tempList = cache.get(uuid);

            return CompletableFuture.supplyAsync(() -> {
                AlliancePlugin.printThread("in completable Future");
                for (var list : tempList.values()){
                    for (var block: list){
                        var tempV3 = BlockVector3.at(block.getX(),block.getY(),block.getZ());
                        if (blocksToReset.contains(tempV3)){
                            block.getState().update();
                        }
                    }
                }
                cache.remove(uuid);
                return null;
            }, AlliancePlugin.pool);
        }
        return null;
    }
}
