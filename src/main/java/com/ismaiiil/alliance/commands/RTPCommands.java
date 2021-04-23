package com.ismaiiil.alliance.commands;


import com.ismaiiil.alliance.features.rtp.RTPManager;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.ismaiiil.alliance.commands.ACommandManager.sendHelpIfPermission;
import static com.ismaiiil.alliance.commands.AllCommandsConstants.*;

@Command(RTP)
@Permission(RTPCommands.PERM_BASIC)
public class RTPCommands {
    public static final String PERM_BASIC = "alliance.rtp.basic";
    @Subcommand("help")
    public static void help(CommandSender sender){
        sender.sendMessage(ACommandManager.getHelpHeader(RTP));
        sendHelpIfPermission(sender,PERM_BASIC, "/rtp help ---- shows this help");
        sendHelpIfPermission(sender,PERM_BASIC, "/rtp ---- randomly teleports you around the world");
        sendHelpIfPermission(sender,PERM_BASIC, "/rtp <linkID>---- teleports you to a friends recent rtp location");
    }

    @Default
    public static void rtp(Player player){
        RTPManager.rtp(player);
    }

    @Default
    public static void rtp(Player player, @AStringArgument String linkID){
        RTPManager.tpUsingTpLink(player, linkID);
    }

}
