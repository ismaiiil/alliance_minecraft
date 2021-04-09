package com.ismaiiil.alliance.Utils.Scoreboard;

import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;

public enum EnumObjective {
    BALANCE("Balance",
            false,
            Material.STICK),

    WAR(    "War",
            false,
             Material.WOODEN_AXE) ,

    CONSTANTS("CONSTANTS",
            true,
             null);
    @Getter
    private final String title;
    @Getter
    private final boolean isIgnored;
    @Getter
    @Setter
    private Material scoreboardItem;

    //TODO use generic type to store data in a DATA CLASS, a bit like PLayerData,
    //for example BALANCE.balance data War.warData etc...

    EnumObjective(final String title,final boolean isIgnored, Material scoreboardItem) {
        this.title = title;
        this.isIgnored = isIgnored;
        this.scoreboardItem = scoreboardItem;
    }


    public static EnumObjective[] myValues(){
        var values = values();
        return (Arrays.stream(values).filter(_eo -> !_eo.isIgnored).toArray(EnumObjective[]::new));
    }



}
