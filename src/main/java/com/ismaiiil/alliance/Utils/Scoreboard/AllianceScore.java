package com.ismaiiil.alliance.Utils.Scoreboard;

import lombok.NonNull;
import lombok.var;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.Objects;

public class AllianceScore {
    private Score scoreText;
    private Score scoreValue;
    private final int row;
    private String value;


    private final String DELIMITER;
    private final EnumScore myScoreData;


    public AllianceScore(Objective objective, EnumScore myScoreData, int row){
        this.DELIMITER = myScoreData.getDelimiter();
        this.myScoreData = myScoreData;
        this.row = row;

        if (myScoreData.isOneLiner()){
            scoreText = objective.getScore(myScoreData.getScoreText() + DELIMITER + myScoreData.getScoreValueColor() + myScoreData.getDefaultScoreValue());
            scoreText.setScore(row);
        }else{
            scoreText = objective.getScore(myScoreData.getScoreText()+ DELIMITER);
            scoreText.setScore(row);

            scoreValue = objective.getScore(myScoreData.getScoreValueColor() + myScoreData.getDefaultScoreValue());
            scoreValue.setScore(row-1);
        }

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
        String scoreEntry;
        var objective = scoreText.getObjective();

        if (myScoreData.isOneLiner()) {
            //split the score at the "colon" mark
            scoreEntry = scoreText.getEntry();
            Objects.requireNonNull(objective.getScoreboard()).resetScores(scoreEntry);

            scoreText = objective.getScore(myScoreData.getScoreText() + DELIMITER + myScoreData.getScoreValueColor() + value);
            scoreText.setScore(row);
            this.value = value;

        }else{
            scoreEntry = scoreValue.getEntry();
            Objects.requireNonNull(objective.getScoreboard()).resetScores(scoreEntry);

            scoreValue = objective.getScore(myScoreData.getScoreValueColor() + value);
            scoreValue.setScore(row-1);
            this.value = value;

        }


    }
}
