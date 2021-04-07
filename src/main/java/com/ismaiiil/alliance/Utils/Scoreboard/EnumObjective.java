package com.ismaiiil.alliance.Utils.Scoreboard;

import lombok.Getter;

public enum EnumObjective {
    BALANCE("Balance",false), WAR("War",false) , CONSTANTS("CONSTANTS",true);
    @Getter
    private final String title;
    @Getter
    private final boolean isIgnored;

    EnumObjective(final String title,final boolean isIgnored) {
        this.title = title;
        this.isIgnored = isIgnored;
    }




}
