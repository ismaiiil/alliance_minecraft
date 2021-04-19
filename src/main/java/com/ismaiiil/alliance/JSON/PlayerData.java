package com.ismaiiil.alliance.JSON;

import lombok.ToString;

import java.io.Serializable;

@ToString
public class PlayerData implements Serializable {
    public int regionsCreated = 0;
    public int balance = 0;
    public int usedBalance = 0;

    public PlayerData() {}
}
