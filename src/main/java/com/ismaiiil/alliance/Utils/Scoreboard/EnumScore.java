package com.ismaiiil.alliance.Utils.Scoreboard;

import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum EnumScore { //Note: order of enum affects the order of the scoreboard
    //------------------------------------------------------------------------------------------------------------------
    //Constant objective scores (USED FOR SPACING)
    //------------------------------------------------------------------------------------------------------------------
    _SPACER_       ( EnumObjective.CONSTANTS,
                    "-------",
                    ChatColor.YELLOW,
                    ":",
                    "-------",
                    null,
                    true),

    //------------------------------------------------------------------------------------------------------------------
    //balance objective scores
    //------------------------------------------------------------------------------------------------------------------
    BALANCE_AMOUNT( EnumObjective.BALANCE,
                    "Balance left",
                    ChatColor.GREEN,
                    ":",
                    "Zero",
                    ChatColor.AQUA,
                    false),


    SPACER1     (_SPACER_, " 1"),


    USED_BALANCE(   EnumObjective.BALANCE,
                    "Balance used",
                    ChatColor.YELLOW,
                    ":",
                    "Zero",
                    ChatColor.GREEN,
                    false),

    SPACER2     (_SPACER_, " 2"),

    NUM_CLAIMS(     EnumObjective.BALANCE,
                    "Claims made",
                    ChatColor.RED,
                    ":",
                    "None",
                   null,
                    true),

    //------------------------------------------------------------------------------------------------------------------
    //War objective scores
    //------------------------------------------------------------------------------------------------------------------

    CURRENT_WAR(    EnumObjective.WAR,
                    "Ongoing war",
                    ChatColor.YELLOW,
                    ":",
                    "None",
                    ChatColor.BLUE,
                    false);

    private final EnumObjective enumObjective;
    private final String scoreText;
    private final ChatColor scoreTextColor;
    private final String defaultScoreValue;
    private final ChatColor scoreValueColor;
    private final boolean isOneLiner;
    private final String delimiter;



    public static Set<EnumScore> balObjScore  = EnumSet.range(BALANCE_AMOUNT, NUM_CLAIMS); // UPDATE THIS WHEN ADDING SCORES
    public static Set<EnumScore> warObjScores = EnumSet.range(CURRENT_WAR, CURRENT_WAR); // UPDATE "

    EnumScore(EnumObjective enumObjective, String scoreText, ChatColor scoreTextColor,String delimiter, String defaultScoreValue, ChatColor scoreValueColor, boolean isOneLiner) {
        this.enumObjective = enumObjective;
        this.scoreText = scoreText;
        this.scoreTextColor = scoreTextColor;
        this.defaultScoreValue = defaultScoreValue;
        this.scoreValueColor = scoreValueColor;
        this.isOneLiner = isOneLiner;
        this.delimiter = delimiter;
    }

    EnumScore(EnumScore enumscore, String delimiter){
        //var previous = values()[ordinal() > 0 ? ordinal()  - 1 : 0];
        this.enumObjective = EnumObjective.BALANCE;
        scoreText = enumscore.scoreText;
        scoreTextColor = enumscore.scoreTextColor;
        this.delimiter = delimiter;
        defaultScoreValue = enumscore.defaultScoreValue;
        scoreValueColor = enumscore.scoreValueColor;
        isOneLiner = enumscore.isOneLiner;
    }

    public String getScoreText() {
        return scoreTextColor + scoreText;
    }


}
