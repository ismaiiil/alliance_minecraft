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

    private static AllianceScoreboardManager inst;
    private final ScoreboardManager bukkitManager;

    private final static String DUMMY = "dummy";

    //player name - Scoreboard
    private HashMap<String, Scoreboard> playerScoreboards = new HashMap<>();
    //player name - AllianceObjective
    private HashMap<String, AllianceObjective> playerObjectives = new HashMap<>();


    private AllianceScoreboardManager() {
        bukkitManager = Bukkit.getScoreboardManager();

    }

    public static AllianceScoreboardManager getInstance() {
        if (inst == null){
            inst = new AllianceScoreboardManager();
        }
        return inst;
    }

    public AllianceObjective getAOBjectiveByName(String playerName){
        return playerObjectives.get(playerName);
    }

    public Scoreboard getPlayerScoreboard(Player player){
        var _obj = playerScoreboards.get(player.getName());
        if (_obj != null) {
            return _obj;
        }else{
            AlliancePlugin.getInstance().getLogger().log(Level.WARNING, "The scoreboard for player " + player + " does not exist");
        }
        return null;
    }

    public void setPlayerScoreboard(Player player){
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

    public AllianceObjective getPlayerObjectives(Player player){
        return playerObjectives.get(player.getName()) ;
    }

    public void updateAllPlayerScores(Player player, EnumObjective enumObjective){
        var _ao = playerObjectives.get(player.getName());
        EnumScore previous = null;
        if(_ao.getActiveObjective() == enumObjective){

            var scores = EnumScoreConstants.allScores.get(enumObjective);
            for (EnumScore enumScore : scores){
                enumScore.updateLinkedValue(player);
            }
        }

    }

    public void updatePlayerScore(Player player, EnumScore enumScore){
        var _ao = playerObjectives.get(player.getName());
        if(_ao.getActiveObjective() == enumScore.getEnumObjective()){
            enumScore.updateLinkedValue(player);
        }

    }

    public void setPlayerSidebar(Player player,EnumObjective enumObjective){
        var _ao = playerObjectives.get(player.getName());
        _ao.changeSideBarObjective(enumObjective);
    }



    public void resetPlayerScoreboard(Player player){
        player.setScoreboard(bukkitManager.getNewScoreboard());
    }

    //TODO REMOVE THIS LATER
    public void deletePlayerScoreboard(Player player){
        resetPlayerScoreboard(player);
        playerScoreboards.remove(player.getName());
        playerObjectives.remove(player.getName());
    }


    private Objective addDummyObjective(Scoreboard thePlayerScoreboard, EnumObjective myObjective){
        return thePlayerScoreboard.registerNewObjective(myObjective.getTitle(), DUMMY, createObjectiveTextComponent(myObjective));
    }

    private TextComponent createObjectiveTextComponent(EnumObjective myObjective){
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
