package com.ismaiiil.alliance.Utils.Scoreboard;

import lombok.var;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class  AllianceObjective {

    public Objective theObjective;

    public ArrayList<AllianceScore> scores = new ArrayList<>();

    public AllianceObjective(Objective theObjective, EnumObjective objectiveType){
        this.theObjective = theObjective;

        var row = 1;
        for (EnumScore myScoreData:EnumScore.balObjScore) {
            scores.add(new AllianceScore(theObjective,myScoreData, row ));
            row += 1;
        }
    }




    //TODO ADD COLON IN SCORE NAMES WHEN CREATING
    //


}
