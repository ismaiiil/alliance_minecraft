package com.ismaiiil.alliance.CommandPackage;


import com.ismaiiil.alliance.CommandPackage.Commands.BlockBalanceCommands;
import com.ismaiiil.alliance.CommandPackage.Commands.ClaimCommands;
import dev.jorel.commandapi.CommandAPI;


public class ACommandManager {


    public static void init(){
        CommandAPI.registerCommand(BlockBalanceCommands.class);
        CommandAPI.registerCommand(ClaimCommands.class);
    }


}
