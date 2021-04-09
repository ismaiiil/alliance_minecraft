package com.ismaiiil.alliance.Utils.Scoreboard;

import lombok.var;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.ismaiiil.alliance.Utils.Scoreboard.Constants.*;


public class  AllianceObjective {

    private final HashMap<EnumObjective ,Objective> objectives;

    public HashMap<EnumScore, AllianceScore> scores = new HashMap<>();

    private EnumObjective activeObjective;

    public AllianceObjective(HashMap<EnumObjective ,Objective> objectives){


        this.objectives = objectives;
        for (EnumObjective enumObjective:objectives.keySet()) {
            int row = lineCountScores.get(enumObjective);
            for (EnumScore enumScore:EnumScore.values()) {
                if (enumScore.getEnumObjective() == enumObjective){
                    var objective = objectives.get(enumObjective);
                    scores.put( enumScore ,(new AllianceScore(objective,enumScore, row )));
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
        activeObjective = enumObjective;
        var _obj = objectives.get(enumObjective);
        _obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public EnumObjective getActiveObjective(){
        return activeObjective;
    }

    public void changeScoreValue(EnumScore enumScore, String value){
        var _as = scores.get(enumScore);
        _as.changeScoreValue(value);

    }



}
