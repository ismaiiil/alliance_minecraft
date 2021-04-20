package com.ismaiiil.alliance.commands;

import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.scoreboard.AllianceScoreboardManager;
import com.ismaiiil.alliance.scoreboard.EnumScore;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import lombok.var;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


@Command("blocks")
@Permission("alliance.blocks.basic")
public class BlockBalanceCommands {

    @Default
    public static void blocks(CommandSender sender){
        sender.sendMessage("--- blocks help ---");
        sender.sendMessage("/blocks - Show this help");
        sender.sendMessage("/blocks <player> - displays the amount of blocks <player> has");
        sender.sendMessage("/blocks add <player> <amount> - add <amount> blocks to <player>'s balance");
    }

    @Default
    @Permission("alliance.blocks.info")
    public static void blocks(CommandSender sender,@APlayerArgument Player player){
        //get player score in PlayerJsonData
        var playerData = AlliancePlugin.getPlayerData(player);

        sender.sendMessage("The player " + player.getName() + " has " + playerData.toString());

    }

    @Subcommand("add")
    @Permission("alliance.blocks.add")
    public static void blocksAdd(CommandSender sender,@APlayerArgument Player player, @AIntegerArgument Integer amount){
        //get player score in PlayerJsonData
        var playerData = AlliancePlugin.getPlayerData(player);
        playerData.balance += amount;
        AllianceScoreboardManager.updatePlayerScore(player, EnumScore.BALANCE_CURRENT);

        player.sendMessage(sender.getName() + " gave you " + amount + " blocks") ;
        sender.sendMessage("You gave " + player.getName() + " " + amount + " blocks") ;

    }

}
