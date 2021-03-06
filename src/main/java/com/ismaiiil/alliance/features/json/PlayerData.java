package com.ismaiiil.alliance.features.json;

import lombok.ToString;

import java.io.Serializable;

@ToString
public class PlayerData implements Serializable {

    public String playerName = "";
    public int regionsCreated = 0;
    public double balance = 0;
    public double usedBalance = 0;

    public PlayerData(String playerName) {
        this.playerName = playerName;
    }
}
