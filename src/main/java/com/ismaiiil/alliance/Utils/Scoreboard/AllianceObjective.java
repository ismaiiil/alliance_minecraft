package com.ismaiiil.alliance.Utils.Scoreboard;

import lombok.var;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class  AllianceObjective {

    private final HashMap<EnumObjective ,Objective> objectives;

    public HashMap<String, AllianceScore> scores = new HashMap<>();

    public AllianceObjective(HashMap<EnumObjective ,Objective> objectives){
        this.objectives = objectives;

        for (EnumObjective enumObjective:objectives.keySet()) {
            var row = 15;
            for (EnumScore enumScore:EnumScore.values()) {
                if (enumScore.getEnumObjective() == enumObjective){
                    var objective = objectives.get(enumObjective);
                    scores.put( enumScore.name() ,(new AllianceScore(objective,enumScore, row )));
                    if (enumScore.isOneLiner()){
                        row -= 1;
                    }else {
                        row -= 2;
                    }

                }
            }
        }
    }

    public void changeSideBarObjective(EnumObjective enumObjective){
        var _obj = objectives.get(enumObjective);
        _obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void changeScoreValue(EnumScore enumScore, String value){
        var _as = scores.get(enumScore.name());
        _as.changeScoreValue(value);

    }



}
