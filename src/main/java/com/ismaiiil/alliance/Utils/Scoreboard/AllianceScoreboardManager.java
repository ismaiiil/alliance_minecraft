package com.ismaiiil.alliance.Utils.Scoreboard;

import com.ismaiiil.alliance.AlliancePlugin;
import lombok.var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.logging.Level;

import static com.ismaiiil.alliance.Utils.Scoreboard.MyObjectives.BALANCE;
import static com.ismaiiil.alliance.Utils.Scoreboard.MyObjectives.WARS;

public class AllianceScoreboardManager {
    private final ScoreboardManager bukkitManager;

    private final static String DUMMY = "dummy";

    private HashMap<String, Scoreboard> playerScoreboards = new HashMap<>();

    public AllianceScoreboardManager(){
        bukkitManager = Bukkit.getScoreboardManager();

    }

    public Scoreboard getPlayerScoreboard(String playerName){
        var _obj = playerScoreboards.get(playerName);
        if (_obj != null) {
            return _obj;
        }else{
            AlliancePlugin.inst().getLogger().log(Level.SEVERE, "The scoreboard for player " + playerName + " does not exist");
        }
        return null;
    }

    public Scoreboard addPlayerScoreboard(String playerName){
        var defaultScoreboard = bukkitManager.getNewScoreboard();
        addDummyObjective(defaultScoreboard, BALANCE.value);
        addDummyObjective(defaultScoreboard, WARS.value);
        playerScoreboards.put(playerName,defaultScoreboard);
        return defaultScoreboard;
    }

    public void getObjectiveScore(String playerName,String myObjective){
        var _al = new AllianceObjective(getPlayerScoreboard(playerName).getObjective(myObjective));
    }


    public void addDummyObjective(Scoreboard thePlayerScoreboard, String myObjective){
        thePlayerScoreboard.registerNewObjective(myObjective, DUMMY, createObjectiveTextComponent(myObjective));
    }

    public TextComponent createObjectiveTextComponent(String myObjective){
        switch (myObjective){
            case BALANCE.value:
                return Component.text(myObjective)
                        .color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.BOLD,true);
            case WARS.value:
                return Component.text(myObjective)
                        .color(NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC,true);
            default:
                AlliancePlugin.inst().getLogger().log(Level.SEVERE, "The objective " + myObjective + " does not exist");
                return null;
        }
    }

}
