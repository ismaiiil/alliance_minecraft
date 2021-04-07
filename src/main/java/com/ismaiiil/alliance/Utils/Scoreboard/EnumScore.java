package com.ismaiiil.alliance.Utils.Scoreboard;

import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.EnumSet;
import java.util.Set;

import static com.ismaiiil.alliance.Utils.Scoreboard.Constants.*;

class Constants  {
    public static String SPACER = "------";
    public static String OUTLINE = "          ";
    public static String DEFAULT_DELIMITER = ": ";
    public static String ZERO = "Zero";
    public static String NONE = "None";
}

@Getter
public enum EnumScore { //Note: order of enum affects the order of the scoreboard

    //------------------------------------------------------------------------------------------------------------------
    //Constant objective scores (USED FOR SPACING)
    //------------------------------------------------------------------------------------------------------------------
    _SPACER_       ( EnumObjective.CONSTANTS,
                     SPACER,
                    ChatColor.YELLOW.toString() ,
                    DEFAULT_DELIMITER,
                    SPACER,
                    null,
                    true),

    _OUTLINE_       (   EnumObjective.CONSTANTS,
                        OUTLINE,
                        ChatColor.GREEN.toString() + ChatColor.STRIKETHROUGH.toString(),
                        DEFAULT_DELIMITER,
                        OUTLINE,
                        null,
                        true),

    //------------------------------------------------------------------------------------------------------------------
    //balance objective scores
    //------------------------------------------------------------------------------------------------------------------

    OUTLINE1       (_OUTLINE_, "j", EnumObjective.BALANCE),

    BALANCE_CURRENT( EnumObjective.BALANCE,
                    "Balance left",
                    "",
                    DEFAULT_DELIMITER,
                    ZERO,
                    ChatColor.GREEN.toString(),
                    false),


    SPACER1     (_SPACER_, "z", EnumObjective.BALANCE),


    BALANCE_USED(   EnumObjective.BALANCE,
                    "Balance used",
                    "",
                    DEFAULT_DELIMITER,
                    ZERO,
                    ChatColor.RED.toString(),
                    false),

    SPACER2     (_SPACER_, "2", EnumObjective.BALANCE),

    BALANCE_CLAIMS(     EnumObjective.BALANCE,
                    "Claims made",
                    ChatColor.BLUE.toString(),
                    DEFAULT_DELIMITER,
                    "None",
                    ChatColor.LIGHT_PURPLE.toString(),
                    true),

    OUTLINE2       (_OUTLINE_, "2", EnumObjective.BALANCE),

    //------------------------------------------------------------------------------------------------------------------
    //War objective scores
    //------------------------------------------------------------------------------------------------------------------

    WAR_CURRENT(    EnumObjective.WAR,
                    "Ongoing war",
                    ChatColor.GREEN.toString(),
                    DEFAULT_DELIMITER,
                    "None",
                    ChatColor.AQUA.toString(),
                    false);

    private final EnumObjective enumObjective;
    private final String scoreText;
    private final String scoreTextColor;
    private final String defaultScoreValue;
    private final String scoreValueColor;
    private final boolean isOneLiner;
    private final String delimiter;


    public static Set<EnumScore> balObjScore ; // UPDATE THIS WHEN ADDING SCORES
    public static Set<EnumScore> warObjScores; // UPDATE "

    public static int balanceRowCount ; // UPDATE THIS WHEN ADDING SCORES
    public static int warRowCount; // UPDATE "

    EnumScore(EnumObjective enumObjective, String scoreText, String scoreTextColor,String delimiter, String defaultScoreValue, String scoreValueColor, boolean isOneLiner) {
        this.enumObjective = enumObjective;
        this.scoreText = scoreText;
        this.scoreTextColor = scoreTextColor;
        this.defaultScoreValue = defaultScoreValue;
        this.scoreValueColor = scoreValueColor;
        this.isOneLiner = isOneLiner;
        this.delimiter = delimiter;
        //setRowCount();
    }

    //Used for constant duplication
    EnumScore(EnumScore enumscore ,String delimiter, EnumObjective enumObjective){
        this.enumObjective = enumObjective;
        this.scoreText = enumscore.scoreText;
        this.scoreTextColor = enumscore.scoreTextColor;
        this.delimiter = ""; //All we need is a clear line with no delimiters
        this.defaultScoreValue = enumscore.defaultScoreValue +  "§" + delimiter; //this hidden text will help make the difference bet. unique values in scoreboard
        this.scoreValueColor = enumscore.scoreTextColor;
        this.isOneLiner = enumscore.isOneLiner;
        //setRowCount();
    }

    private void setRowCount(){
        switch (this.getEnumObjective()){
            case BALANCE:
                balObjScore.add(this);
                balanceRowCount = this.isOneLiner ? balanceRowCount++ : balanceRowCount + 2;
            case WAR:
                warObjScores.add(this);
                warRowCount = this.isOneLiner ? warRowCount++ : warRowCount + 2;
            case CONSTANTS:
            default:
        }

    }


    public String getScoreText() {
        return scoreTextColor + scoreText;
    }


}
