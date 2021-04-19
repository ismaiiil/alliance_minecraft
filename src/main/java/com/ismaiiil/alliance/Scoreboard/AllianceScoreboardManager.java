package com.ismaiiil.alliance.Scoreboard;

import com.ismaiiil.alliance.AlliancePlugin;
import lombok.var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.logging.Level;


public class AllianceScoreboardManager {

    private static ScoreboardManager bukkitManager;

    private final static String DUMMY = "dummy";

    //player name - Scoreboard
    private static HashMap<String, Scoreboard> playerScoreboards = new HashMap<>();
    //player name - AllianceObjective
    private static HashMap<String, AllianceObjective> playerObjectives = new HashMap<>();


    public static void init() {
        bukkitManager = Bukkit.getScoreboardManager();

    }

    public static AllianceObjective getAOBjectiveByName(String playerName){
        return playerObjectives.get(playerName);
    }

    public static Scoreboard getPlayerScoreboard(Player player){
        var _obj = playerScoreboards.get(player.getName());
        if (_obj != null) {
            return _obj;
        }else{
            AlliancePlugin.getInstance().getLogger().log(Level.WARNING, "The scoreboard for player " + player + " does not exist");
        }
        return null;
    }

    public static void setPlayerScoreboard(Player player){
        var playerScore = getPlayerScoreboard(player);

        if (playerScore == null){
            playerScore = bukkitManager.getNewScoreboard();

            HashMap<EnumObjective ,Objective> objectives = new HashMap<>();

            for (EnumObjective _eo: EnumObjective.myValues()) {
                objectives.put(_eo,addDummyObjective(playerScore, _eo));
            }

            playerObjectives.put(player.getName(),new AllianceObjective(objectives));

            playerScoreboards.put(player.getName(),playerScore);
        }


        player.setScoreboard(playerScore);

    }

    public static AllianceObjective getPlayerObjectives(Player player){
        return playerObjectives.get(player.getName()) ;
    }

    public static void updateAllPlayerScores(Player player, EnumObjective enumObjective){
        var _ao = playerObjectives.get(player.getName());
        EnumScore previous = null;
        if(_ao.getActiveObjective() == enumObjective){

            var scores = EnumScoreConstants.allScores.get(enumObjective);
            for (EnumScore enumScore : scores){
                enumScore.updateLinkedValue(player);
            }
        }

    }

    public static void updatePlayerScore(Player player, EnumScore enumScore){
        var _ao = playerObjectives.get(player.getName());
        if(_ao.getActiveObjective() == enumScore.getEnumObjective()){
            enumScore.updateLinkedValue(player);
        }

    }

    public static void setPlayerSidebar(Player player,EnumObjective enumObjective){
        var _ao = playerObjectives.get(player.getName());
        _ao.changeSideBarObjective(enumObjective);
    }

    public static void resetPlayerScoreboard(Player player){
        player.setScoreboard(bukkitManager.getNewScoreboard());
    }

    public static void deletePlayerScoreboard(Player player){
        resetPlayerScoreboard(player);
        playerScoreboards.remove(player.getName());
        playerObjectives.remove(player.getName());
    }

    private static Objective addDummyObjective(Scoreboard thePlayerScoreboard, EnumObjective myObjective){
        return thePlayerScoreboard.registerNewObjective(myObjective.getTitle(), DUMMY, createObjectiveTextComponent(myObjective));
    }

    private static TextComponent createObjectiveTextComponent(EnumObjective myObjective){
        switch (myObjective){
            case BALANCE:
                return Component.text(myObjective.getTitle())
                        .color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.BOLD,true);
            case WAR:
                return Component.text(myObjective.getTitle())
                        .color(NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC,true);
            default:
                AlliancePlugin.getInstance().getLogger().log(Level.SEVERE, "The objective " + myObjective + " does not exist");
                return null;
        }
    }

}
