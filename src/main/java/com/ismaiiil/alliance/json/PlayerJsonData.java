package com.ismaiiil.alliance.json;

import com.ismaiiil.alliance.AlliancePlugin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;

public class PlayerJsonData implements Serializable {

    public HashMap<String, PlayerData> players = new HashMap<>();

    public PlayerJsonData(){}

    public void createPlayerData(String playerName, int defaultBalance){
        PlayerData _be = new PlayerData();
        _be.balance = defaultBalance;
        _be.regionsCreated = 0;
        if (!players.containsKey(playerName)){
            players.put(playerName,_be);
            return;
        }
        else{
            AlliancePlugin.getInstance().getLogger().log(Level.SEVERE, "You are trying to create a balance for a player that already have a balance");
        }
        return;
    }

    public PlayerData getPlayerData(String playerName){

        if (players.containsKey(playerName)){
            return players.get(playerName);
        }else{
            AlliancePlugin.getInstance().getLogger().log(Level.SEVERE, "The player " + playerName + " isn't in the balance.json file");
            return null;
        }

    }



}
