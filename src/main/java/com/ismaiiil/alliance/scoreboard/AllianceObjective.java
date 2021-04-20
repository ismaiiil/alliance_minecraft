package com.ismaiiil.alliance.scoreboard;

import lombok.var;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.*;

import static com.ismaiiil.alliance.scoreboard.EnumScoreConstants.*;


public class  AllianceObjective {

    private final HashMap<EnumObjective ,Objective> objectives;

    public HashMap<EnumScore, AllianceScore> scores = new HashMap<>();

    private EnumObjective activeObjective;

    public AllianceObjective(HashMap<EnumObjective ,Objective> objectives){


        this.objectives = objectives;
        for (Map.Entry<EnumObjective,Objective> set:objectives.entrySet()) {
            var enumObjective = set.getKey();
            var objective = set.getValue();
            int row = allScoresCount.get(enumObjective);
            for (EnumScore enumScore:allScores.get(enumObjective)) {
                scores.put( enumScore ,(new AllianceScore(objective,enumScore, row )));
                if (enumScore.isOneLiner()){
                    row -= 1;
                }else {
                    row -= 2;
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
