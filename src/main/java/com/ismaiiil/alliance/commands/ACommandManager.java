package com.ismaiiil.alliance.commands;


import dev.jorel.commandapi.CommandAPI;
import lombok.var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;


public class ACommandManager {

    public static ArrayList<String> commands = new ArrayList<>();
    private static final int contentLinesPerPage = 10;

    public static void init(){
        CommandAPI.registerCommand(BlockBalanceCommands.class);
        CommandAPI.registerCommand(ClaimCommands.class);
        CommandAPI.registerCommand(AllianceCommands.class);
        CommandAPI.registerCommand(RTPCommands.class);
        CommandAPI.registerCommand(ConfigCommands.class);
    }

    public static Component getHelpHeader(String command){
        commands.add(command);
        return Component.text(String.format("------ %s help ------", command ))
                .color(NamedTextColor.YELLOW)
                ;
    }

    public static void paginateArrayComponent(CommandSender sender, ArrayList<Component> list, int page, String title) {
        int totalPageCount = 1;

        if((list.size() % contentLinesPerPage) == 0)
        {
            if(list.size() > 0)
            {
                totalPageCount = list.size() / contentLinesPerPage;
            }
        }
        else
        {
            totalPageCount = (list.size() / contentLinesPerPage) + 1;
        }

        if(page <= totalPageCount && page > 0)
        {
            sender.sendMessage(Component.text("----------------------------------------"));
            sender.sendMessage(Component.text( String.format(" %s - Page (%s of %s)",title,page,totalPageCount )).color(TextColor.color(0xffa500)));
            sender.sendMessage(Component.text( "----------------------------------------"));

            if(list.isEmpty())
            {
                sender.sendMessage(Component.text("----------------------------------------"));
            }
            else
            {
                int i = 0, k = 0;
                page--;

                for (Component entry : list)
                {
                    k++;
                    if ((((page * contentLinesPerPage) + i + 1) == k) && (k != ((page * contentLinesPerPage) + contentLinesPerPage + 1)))
                    {
                        i++;
                        sender.sendMessage(entry);
                    }
                }
            }

            sender.sendMessage(Component.text("----------------------------------------"));


        }else{
            sender.sendMessage(Component.text(String.format("There are only %s pages", totalPageCount)).color(NamedTextColor.RED));
        }
    }

    public static ArrayList<Component> stringArrayToComponent(ArrayList<String> strings){
        var array = new ArrayList<Component>();
        strings.forEach(s -> array.add(Component.text(s)) );
        return array;
    }

    public static void sendHelpIfPermission(CommandSender sender, String permission, String help){
        if (sender.hasPermission(permission)){
            sender.sendMessage(help);
        }
    }
}
