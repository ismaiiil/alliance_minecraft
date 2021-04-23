package com.ismaiiil.alliance.features.json;

import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.features.logger.AllianceLogger;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getServer;

public class PlayerJsonData implements Serializable {

    private HashMap<UUID, PlayerData> players = new HashMap<>();

    public PlayerJsonData(){}

    public void createPlayerData(Player player, int defaultBalance){
        PlayerData _be = new PlayerData(player.getName());
        _be.balance = defaultBalance;
        _be.regionsCreated = 0;
        if (!players.containsKey(player.getUniqueId())){
            players.put(player.getUniqueId(),_be);
        }
        else{
            AllianceLogger.log(Level.SEVERE, "You are trying to create a balance for a player that already have a balance");
        }
    }

    public PlayerData getPlayerData(Player player){

        if (players.containsKey(player.getUniqueId())){
            return players.get(player.getUniqueId());
        }else{
            createPlayerData(player, AlliancePlugin.getInstance().defaultBalance);
            AllianceLogger.log(Level.WARNING, "The player " + player.getName() + " isn't in the balance.json file, now added");
            return null;
        }

    }

    public void saveDataToFile(){
        ConfigLoader.saveConfig(AlliancePlugin.playerJsonData,AlliancePlugin.playerJsonFile);
    }

    public void saveDataToFileAsync(){
        getServer().getScheduler().runTaskAsynchronously(AlliancePlugin.getInstance(), () -> ConfigLoader.saveConfig(AlliancePlugin.playerJsonData,AlliancePlugin.playerJsonFile));
    }



}
