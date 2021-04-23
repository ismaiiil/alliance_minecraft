package com.ismaiiil.alliance.commands;

import com.ismaiiil.alliance.AlliancePlugin;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import org.bukkit.command.CommandSender;

import static com.ismaiiil.alliance.commands.AllCommandsConstants.*;
import static com.ismaiiil.alliance.commands.ACommandManager.*;

@Command(CONFIG)
@Permission(ClaimCommands.PERM_BASIC)
public class ConfigCommands {
    public static final String PERM_BASIC = "alliance.config.basic";

    @Subcommand("help")
    public static void help(CommandSender sender){
        sender.sendMessage(getHelpHeader(CONFIG));
        sendHelpIfPermission(sender,PERM_BASIC, "/config help- Show this help");
        sendHelpIfPermission(sender,PERM_BASIC, "/config reload - reload config files into memory");
    }

    @Subcommand("reload")
    public static void reload(CommandSender sender){
        AlliancePlugin.getInstance().reloadConfig();
        AlliancePlugin.getInstance().loadConfigsInMemory();
    }


}
