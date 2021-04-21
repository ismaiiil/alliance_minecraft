package com.ismaiiil.alliance.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import org.bukkit.command.CommandSender;

import static com.ismaiiil.alliance.commands.AllCommandsConstants.ALLIANCE;
import static com.ismaiiil.alliance.commands.AllCommandsConstants.getAllCommandDesc;

@Command(ALLIANCE)
@Permission("alliance.alliance.basic")
public class AllianceCommands {


    private static void help(CommandSender sender, Integer page){
        if (page == null){
            page = 1;
        }
        ACommandManager.paginateArrayComponent(sender,ACommandManager.stringArrayToComponent(getAllCommandDesc(sender)) ,page, "Alliance Help");
    }

    @Default
    @Subcommand("help")
    public static void help(CommandSender sender){
        help(sender, null);
    }

    @Default
    @Subcommand("help")
    public static void helpPage(CommandSender sender, @AIntegerArgument Integer page){
        help(sender, page);
    }



}
