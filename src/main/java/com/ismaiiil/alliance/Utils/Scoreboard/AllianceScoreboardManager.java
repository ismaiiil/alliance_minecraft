package com.ismaiiil.alliance.Utils.Scoreboard;

import com.ismaiiil.alliance.AlliancePlugin;
import lombok.var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.logging.Level;

import static com.ismaiiil.alliance.Utils.Scoreboard.EnumObjective.BALANCE;
import static com.ismaiiil.alliance.Utils.Scoreboard.EnumObjective.WARS;

//import static com.ismaiiil.alliance.Utils.Scoreboard.MyObjectives.BALANCE;
//import static com.ismaiiil.alliance.Utils.Scoreboard.MyObjectives.WARS;

public class AllianceScoreboardManager {
    private final ScoreboardManager bukkitManager;

    private final static String DUMMY = "dummy";

    private HashMap<String, Scoreboard> playerScoreboards = new HashMap<>();

    public AllianceScoreboardManager() {
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
//        addDummyObjective(defaultScoreboard, BALANCE.value);
//        addDummyObjective(defaultScoreboard, WARS.value);
        addDummyObjective(defaultScoreboard, BALANCE);
        addDummyObjective(defaultScoreboard, WARS);
        playerScoreboards.put(playerName,defaultScoreboard);
        return defaultScoreboard;
    }


    public Objective addDummyObjective(Scoreboard thePlayerScoreboard, EnumObjective myObjective){
        return thePlayerScoreboard.registerNewObjective(myObjective.getTitle(), DUMMY, createObjectiveTextComponent(myObjective));
    }

    public TextComponent createObjectiveTextComponent(EnumObjective myObjective){
        switch (myObjective){
            case BALANCE:
                return Component.text(myObjective.getTitle())
                        .color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.BOLD,true);
            case WARS:
                return Component.text(myObjective.getTitle())
                        .color(NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC,true);
            default:
                AlliancePlugin.inst().getLogger().log(Level.SEVERE, "The objective " + myObjective + " does not exist");
                return null;
        }
    }

}
