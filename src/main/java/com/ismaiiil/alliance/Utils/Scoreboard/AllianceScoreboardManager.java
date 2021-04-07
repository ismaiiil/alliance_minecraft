package com.ismaiiil.alliance.Utils.Scoreboard;

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

import static com.ismaiiil.alliance.Utils.Scoreboard.EnumObjective.BALANCE;
import static com.ismaiiil.alliance.Utils.Scoreboard.EnumObjective.WAR;


public class AllianceScoreboardManager {
    private final ScoreboardManager bukkitManager;

    private final static String DUMMY = "dummy";

    private HashMap<String, Scoreboard> playerScoreboards = new HashMap<>();
    private HashMap<String, AllianceObjective> playerObjectives = new HashMap<>();

    public AllianceScoreboardManager() {
        bukkitManager = Bukkit.getScoreboardManager();

    }

    public Scoreboard getPlayerScoreboard(Player player){
        var _obj = playerScoreboards.get(player.getName());
        if (_obj != null) {
            return _obj;
        }else{
            AlliancePlugin.inst().getLogger().log(Level.SEVERE, "The scoreboard for player " + player + " does not exist");
        }
        return null;
    }

    public void addPlayerScoreboard(Player player){
        var defaultScoreboard = bukkitManager.getNewScoreboard();

        HashMap<EnumObjective ,Objective> objectives = new HashMap<>();
//        objectives.put(BALANCE,addDummyObjective(defaultScoreboard, BALANCE));
//        objectives.put(WAR,addDummyObjective(defaultScoreboard, WAR));

        for (EnumObjective _eo: EnumObjective.values()) {
            if (!_eo.isIgnored()){
                objectives.put(_eo,addDummyObjective(defaultScoreboard, _eo));
            }
        }

        playerObjectives.put(player.getName(),new AllianceObjective(objectives));

        playerScoreboards.put(player.getName(),defaultScoreboard);
        player.setScoreboard(defaultScoreboard);

    }

    public void changePlayerScore(Player player,EnumScore enumScore,String value){
        var _ao = playerObjectives.get(player.getName());
        _ao.changeScoreValue(enumScore,value);
    }

    public void setPlayerSidebar(Player player,EnumObjective enumObjective){
        var _ao = playerObjectives.get(player.getName());
        _ao.changeSideBarObjective(enumObjective);
    }

    //TODO REMOVE THIS LATER
    public void DEBUG_DELETE(Player player){
        player.setScoreboard(bukkitManager.getNewScoreboard());

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
                AlliancePlugin.inst().getLogger().log(Level.SEVERE, "The objective " + myObjective + " does not exist");
                return null;
        }
    }

}
