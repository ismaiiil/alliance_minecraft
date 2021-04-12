package com.ismaiiil.alliance.Scoreboard;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class AllianceScore {
    private Score scoreText;
    private Score scoreValue;
    private Team team;
    private final int row;
    private String value;


    private final String DELIMITER;
    private final EnumScore myScoreData;

    //private static final String[] delimiters = new String[]{"§0", "§1","§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f"  };


    public AllianceScore(Objective objective, EnumScore myScoreData, int row){
        this.DELIMITER = myScoreData.getDelimiter();
        this.myScoreData = myScoreData;
        this.row = row;

        //generate hidden text entry
        int twoLineRow = row-1;
        StringBuilder twoLineEntry = getTeamEntry(twoLineRow);
        StringBuilder oneLineEntry = getTeamEntry(row);

        String uniqueId = RandomStringUtils.random(14);

        if (myScoreData.isOneLiner()){
            team = objective.getScoreboard().registerNewTeam(uniqueId);
            team.addEntry(myScoreData.getScoreText() + DELIMITER + oneLineEntry.toString());
            team.setPrefix("");
            team.setSuffix(myScoreData.getScoreValueColor() + myScoreData.getDefaultScoreValue());

            //scoreText = objective.getScore(myScoreData.getScoreText() + DELIMITER + myScoreData.getScoreValueColor() + myScoreData.getDefaultScoreValue());
            scoreText = objective.getScore(myScoreData.getScoreText() + DELIMITER + oneLineEntry.toString()) ;
            scoreText.setScore(row);
        }else{

            scoreText = objective.getScore(myScoreData.getScoreText()+ DELIMITER);
            scoreText.setScore(row);

            team = objective.getScoreboard().registerNewTeam(uniqueId);
            team.addEntry(twoLineEntry.toString());
            team.setPrefix("");
            team.setSuffix(myScoreData.getScoreValueColor() + myScoreData.getDefaultScoreValue());

            //scoreValue = objective.getScore(myScoreData.getScoreValueColor() + myScoreData.getDefaultScoreValue());
            scoreValue = objective.getScore(twoLineEntry.toString());
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
//
//    @SuppressWarnings("unchecked")
//    public <T> T getScoreValue(Class<T> type){
//        String returnString = value;
//        final String intClass = "Integer";
//        final String doubleClass = "Double";
//        final String StringClass = "String";
//        try {
//            switch (type.getName()) {
//                case StringClass:
//                    return (T) returnString;
//                case intClass:
//                    returnString = returnString.replaceAll("\\s+","");
//                    Integer returnInt = Integer.parseInt(returnString);
//                    return  (T) returnInt ;
//                case doubleClass:
//                    returnString = returnString.replaceAll("\\s+","");
//                    Double returnDouble = Double.parseDouble(returnString);
//                    return  (T) returnDouble ;
//                default:
//                    throw new ClassNotFoundException();
//            }
//        }catch ( ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public void changeScoreValue(String value){
        team.setSuffix(myScoreData.getScoreValueColor() + value);
        this.value = value;
    }
}
