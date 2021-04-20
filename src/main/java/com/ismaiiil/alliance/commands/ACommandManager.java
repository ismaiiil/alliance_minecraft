package com.ismaiiil.alliance.commands;


import dev.jorel.commandapi.CommandAPI;


public class ACommandManager {


    public static void init(){
        CommandAPI.registerCommand(BlockBalanceCommands.class);
        CommandAPI.registerCommand(ClaimCommands.class);
    }


}
