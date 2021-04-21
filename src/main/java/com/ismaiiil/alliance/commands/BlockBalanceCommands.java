package com.ismaiiil.alliance.commands;

import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.scoreboard.AllianceScoreboardManager;
import com.ismaiiil.alliance.scoreboard.EnumScore;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.ADoubleArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import lombok.var;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static com.ismaiiil.alliance.commands.AllCommandsConstants.*;
import static com.ismaiiil.alliance.commands.ACommandManager.*;

@Command(BLOCKS)
@Permission(BlockBalanceCommands.PERM_BASIC)
public class BlockBalanceCommands {
    public static final String PERM_BASIC = "alliance.blocks.basic";
    public static final String PERM_INFO = "alliance.blocks.info";
    public static final String PERM_ADD = "alliance.blocks.add";
    @Default
    @Subcommand("help")
    public static void blocks(CommandSender sender){
        sender.sendMessage(ACommandManager.getHelpHeader("blocks"));
        sendHelpIfPermission(sender,PERM_BASIC, "/blocks help - Show this help");
        sendHelpIfPermission(sender,PERM_INFO, "/blocks <player> - displays the amount of blocks <player> has");
        sendHelpIfPermission(sender,PERM_ADD, "/blocks add <player> <amount> - add <amount> blocks to <player>'s balance");
    }

    @Default
    @Permission(PERM_INFO)
    public static void blocks(CommandSender sender,@APlayerArgument Player player){
        //get player score in PlayerJsonData
        var playerData = AlliancePlugin.getPlayerData(player);

        sender.sendMessage("The player " + player.getName() + " has " + playerData.toString());

    }

    @Subcommand("add")
    @Permission(PERM_ADD)
    public static void blocksAdd(CommandSender sender,@APlayerArgument Player player, @ADoubleArgument Double amount){
        //get player score in PlayerJsonData
        var playerData = AlliancePlugin.getPlayerData(player);
        playerData.balance += amount;
        AllianceScoreboardManager.updatePlayerScore(player, EnumScore.BALANCE_CURRENT);

        player.sendMessage(sender.getName() + " gave you " + amount + " blocks") ;
        sender.sendMessage("You gave " + player.getName() + " " + amount + " blocks") ;

    }

}
