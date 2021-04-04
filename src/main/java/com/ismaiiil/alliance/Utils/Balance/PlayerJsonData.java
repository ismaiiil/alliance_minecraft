package com.ismaiiil.alliance.Utils.Balance;

import com.ismaiiil.alliance.AlliancePlugin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;

public class PlayerJsonData implements Serializable {

    public HashMap<String, PlayerData> players = new HashMap<>();

    public PlayerJsonData(){}

    public PlayerData createPlayerBalance(String playerName, int defaultBalance){
        PlayerData _be = new PlayerData();
        _be.balance = defaultBalance;
        _be.regionCount = 0;
        if (!players.containsKey(playerName)){
            players.put(playerName,_be);
            return _be;
        }
        else{
            AlliancePlugin.inst().getLogger().log(Level.SEVERE, "You are trying to create a balance for a player that already have a balance");
        }
        return null;
    }

    public PlayerData getPlayerData(String playerName){
        PlayerData _be = players.get(playerName);
        if (_be != null) {
            return _be;
        }else{
            AlliancePlugin.inst().getLogger().log(Level.SEVERE, "The player " + playerName + " isn't in the balance.json file");
        }
        return null;
    }



}
