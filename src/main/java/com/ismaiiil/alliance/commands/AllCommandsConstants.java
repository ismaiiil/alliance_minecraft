package com.ismaiiil.alliance.commands;

import lombok.var;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class AllCommandsConstants {
    private static class CONSTANTS{
        public static String DOT = ".";
        public static String BASIC = "basic";
        public static String DESC_PREFIX = "DESC_";
    }

    public static final String ALLIANCE = "alliance";
    public static final String DESC_ALLIANCE = " ---- Help Command (/alliance help)";
    public static final String CLAIMS = "claims";
    public static final String DESC_CLAIMS = " ---- Claim commands, manage your territory";
    public static final String BLOCKS = "blocks";
    public static final String DESC_BLOCKS = " ---- Manage player block balance";

    public static ArrayList<String> getAllCommandDesc(CommandSender sender){
        Field[] interfaceFields= AllCommandsConstants.class.getFields();
        var strings = new ArrayList<String>();
        String tempString ="";
        for(Field f:interfaceFields) {
            try {
                var string = (String) f.get( AllCommandsConstants.class);
                if (!f.getName().contains( CONSTANTS.DESC_PREFIX)){
                    var commandPermission = ALLIANCE + CONSTANTS.DOT + string + CONSTANTS.DOT + CONSTANTS.BASIC;
                    //check if he has the basic permission to access the commands and subcommands
                    if (sender.hasPermission(commandPermission)){
                        tempString = string;
                    }
                }else{
                    if (!tempString.isEmpty()){
                        strings.add(tempString + " " + string);
                        tempString = "";
                    }
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return strings;
    }



}
