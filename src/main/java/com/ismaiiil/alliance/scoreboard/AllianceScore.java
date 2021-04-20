package com.ismaiiil.alliance.scoreboard;

import com.ismaiiil.alliance.AlliancePlugin;
import lombok.var;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

@SuppressWarnings("deprecation")
public class AllianceScore {
    private Team team;
    private final EnumScore myScoreData;

    //private static final String[] delimiters = new String[]{"§0", "§1","§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f"  };


    public AllianceScore(Objective objective, EnumScore myScoreData, int row){
        String DELIMITER = myScoreData.getDelimiter();
        this.myScoreData = myScoreData;
        Score scoreText;

        //generate hidden text entry
        int twoLineRow = row-1;
        StringBuilder twoLineEntry = getTeamEntry(twoLineRow);
        StringBuilder oneLineEntry = getTeamEntry(row);

        String uniqueId = RandomStringUtils.random(14);


        if (myScoreData.isOneLiner()){
            var sb = objective.getScoreboard();
            if (sb == null){
                AlliancePlugin.getInstance().getLogger().log(Level.SEVERE, "The scoreboard in " + objective.getName() + " does not exist");
                return;
            }
            team = sb.registerNewTeam(uniqueId);

            team.addEntry(myScoreData.getScoreText() + DELIMITER + oneLineEntry.toString());
            team.setPrefix("");
            team.setSuffix(myScoreData.getScoreValueColor() + myScoreData.getDefaultScoreValue());

            //scoreText = objective.getScore(myScoreData.getScoreText() + DELIMITER + myScoreData.getScoreValueColor() + myScoreData.getDefaultScoreValue());
            scoreText = objective.getScore(myScoreData.getScoreText() + DELIMITER + oneLineEntry.toString()) ;
            scoreText.setScore(row);
        }else{

            scoreText = objective.getScore(myScoreData.getScoreText()+ DELIMITER);
            scoreText.setScore(row);

            var sb = objective.getScoreboard();
            if (sb == null){
                AlliancePlugin.getInstance().getLogger().log(Level.SEVERE, "The scoreboard in " + objective.getName() + " does not exist");
                return;
            }
            team = sb.registerNewTeam(uniqueId);
            team.addEntry(twoLineEntry.toString());
            team.setPrefix("");
            team.setSuffix(myScoreData.getScoreValueColor() + myScoreData.getDefaultScoreValue());

            //scoreValue = objective.getScore(myScoreData.getScoreValueColor() + myScoreData.getDefaultScoreValue());
            Score scoreValue = objective.getScore(twoLineEntry.toString());
            scoreValue.setScore(row-1);
        }

    }

    @NotNull
    private StringBuilder getTeamEntry(int finalRow) {
        char[] intRowToChars =  String.valueOf(finalRow).toCharArray();
        StringBuilder teamEntry = new StringBuilder();
        for (char _c:intRowToChars) {
            teamEntry.append("§");
            teamEntry.append(_c);
        }
        return teamEntry;
    }


    public void changeScoreValue(String value){
        team.setSuffix(myScoreData.getScoreValueColor() + value);
    }
}
