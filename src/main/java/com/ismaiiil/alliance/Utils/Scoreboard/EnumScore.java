package com.ismaiiil.alliance.Utils.Scoreboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.EnumSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum EnumScore { //Note: order of enum affects the order of the scoreboard
    //------------------------------------------------------------------------------------------------------------------
    //balance objective scores
    //------------------------------------------------------------------------------------------------------------------
    BALANCE_AMOUNT( EnumObjective.BALANCE,
                    "Your Balance left",
                    ChatColor.GREEN,
                    "Zero",
                    ChatColor.AQUA,
                    false),

    USED_BALANCE(   EnumObjective.BALANCE,
                    "Your block balance in use",
                    ChatColor.YELLOW,
                    "Zero",
                    ChatColor.GREEN,
                    false),

    NUM_CLAIMS(     EnumObjective.BALANCE,
                    "Your Number of Claims",
                    ChatColor.RED,
                    "None",
                   null,
                    true),

    //------------------------------------------------------------------------------------------------------------------
    //War objective scores
    //------------------------------------------------------------------------------------------------------------------

    CURRENT_WAR(    EnumObjective.WARS,
                    "Ongoing war",
                    ChatColor.YELLOW,
                    "None",
                    ChatColor.BLUE,
                    false);

    private final EnumObjective enumObjective;
    private final String scoreText;
    private final ChatColor scoreTextColor;
    private final String defaultScoreValue;
    private final ChatColor scoreValueColor;
    private final boolean isOneLiner;

    public static Set<EnumScore> balObjScore  = EnumSet.range(BALANCE_AMOUNT, NUM_CLAIMS); // UPDATE THIS WHEN ADDING SCORES
    public static Set<EnumScore> warObjScores = EnumSet.range(CURRENT_WAR, CURRENT_WAR); // UPDATE "

    public String getScoreText() {
        return scoreTextColor + scoreText;
    }

}
