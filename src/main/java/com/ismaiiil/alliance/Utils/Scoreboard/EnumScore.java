package com.ismaiiil.alliance.Utils.Scoreboard;

import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.Utils.ConfigLoading.JSON.PlayerData;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.var;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

import static com.ismaiiil.alliance.Utils.Scoreboard.Constants.*;

class Constants {
    public static HashMap<EnumObjective, ArrayList<EnumScore>> objToScores = new HashMap<>(); // UPDATE THIS WHEN ADDING SCORES
    public static HashMap<EnumObjective, Integer> lineCountScores = new HashMap<>();

    public static int maxLineCount = 15;

    public static String SPACER = "------";
    public static String OUTLINE = "          ";
    public static String DEFAULT_DELIMITER = ": ";
    public static String ZERO = "Zero";
    public static String NONE = "None";

}

class MaxScoreboardLineCountExceeded extends Exception {
    public MaxScoreboardLineCountExceeded() {
        super("Please check if " + EnumScore.class.getName() + " doesn't exceed " + maxLineCount);
    }
}

@Getter
public enum EnumScore { //Note: order of enum affects the order of the scoreboard

    //------------------------------------------------------------------------------------------------------------------
    //Constant objective scores (USED FOR SPACING)
    //------------------------------------------------------------------------------------------------------------------
    _SPACER_(EnumObjective.CONSTANTS,
            SPACER,
            ChatColor.YELLOW.toString(),
            DEFAULT_DELIMITER,
            SPACER,
            null,
            true) { public void updateLinkedValue(Player player) {}},

    _OUTLINE_(EnumObjective.CONSTANTS,
            OUTLINE,
            ChatColor.GREEN.toString() + ChatColor.STRIKETHROUGH.toString(),
            DEFAULT_DELIMITER,
            OUTLINE,
            null,
            true) { public void updateLinkedValue(Player player) {}},

    //------------------------------------------------------------------------------------------------------------------
    //balance objective scores
    //------------------------------------------------------------------------------------------------------------------

    OUTLINE1(_OUTLINE_, "a", EnumObjective.BALANCE) { public void updateLinkedValue(Player player) {}},


    BALANCE_CURRENT(EnumObjective.BALANCE,
            "Balance left",
            "",
            DEFAULT_DELIMITER,
            ZERO,
            ChatColor.GREEN.toString(),
            false) {
        @Override
        public void updateLinkedValue(Player player) {
            PlayerData data = AlliancePlugin.getInstance().playerJsonData.getPlayerData(player.getName());
            changeScoreValue(this,player,String.valueOf(data.balance));
        }
    },


    SPACER1(_SPACER_, "b", EnumObjective.BALANCE){ public void updateLinkedValue(Player player) {}},


    BALANCE_USED(EnumObjective.BALANCE,
            "Balance used",
            "",
            DEFAULT_DELIMITER,
            ZERO,
            ChatColor.RED.toString(),
            false) {
        @Override
        public void updateLinkedValue(Player player) {
            PlayerData data = AlliancePlugin.getInstance().playerJsonData.getPlayerData(player.getName());
            changeScoreValue(this,player,String.valueOf(data.usedBalance));
        }
    },

    SPACER2(_SPACER_, "c", EnumObjective.BALANCE) { public void updateLinkedValue(Player player) {}},

    BALANCE_CLAIMS(EnumObjective.BALANCE,
            "Claims made",
            "",
            DEFAULT_DELIMITER,
            NONE,
            ChatColor.GREEN.toString(),
            true) {
        @Override
        public void updateLinkedValue(Player player) {
            AlliancePlugin plugin = AlliancePlugin.getInstance();
            int regionCount = plugin.defaultRegionManager.getRegionCountOfPlayer(plugin.worldGuardPlugin.wrapPlayer(player));
            changeScoreValue(this,player,String.valueOf(regionCount));
        }
    },

    OUTLINE2(_OUTLINE_, "d", EnumObjective.BALANCE) { public void updateLinkedValue(Player player) {}},

    //------------------------------------------------------------------------------------------------------------------
    //War objective scores
    //------------------------------------------------------------------------------------------------------------------

    WAR_CURRENT(EnumObjective.WAR,
            "Ongoing war",
            ChatColor.GREEN.toString(),
            DEFAULT_DELIMITER,
            NONE,
            ChatColor.AQUA.toString(),
            false) {
        @Override
        public void updateLinkedValue(Player player) {

        }
    };


    public abstract void updateLinkedValue(Player player);

    private final EnumObjective enumObjective;
    private final String scoreText;
    private final String scoreTextColor;
    private final String defaultScoreValue;
    private final String scoreValueColor;
    private final boolean isOneLiner;
    private String delimiter;

    EnumScore(EnumObjective enumObjective, String scoreText, String scoreTextColor, String delimiter, String defaultScoreValue, String scoreValueColor, boolean isOneLiner) {
        this.enumObjective = enumObjective;
        this.scoreText = scoreText;
        this.scoreTextColor = scoreTextColor;
        this.defaultScoreValue = defaultScoreValue;
        this.scoreValueColor = scoreValueColor;
        this.isOneLiner = isOneLiner;
        this.delimiter = delimiter;
        setRowCount(enumObjective);
    }

    //Used for constant duplication
    EnumScore(EnumScore enumscore, String delimiter, EnumObjective enumObjective) {
        this.enumObjective = enumObjective;
        this.scoreText = enumscore.scoreText;
        this.scoreTextColor = enumscore.scoreTextColor;
        //this hidden text will help make the difference bet. unique values in scoreboard
        this.defaultScoreValue = enumscore.defaultScoreValue + "§" + delimiter;
        //All we need is a clear line with no delimiters
        this.delimiter = "";
        this.scoreValueColor = enumscore.scoreTextColor;
        this.isOneLiner = enumscore.isOneLiner;
        setRowCount(enumscore.enumObjective);

    }


    @SneakyThrows
    private void setRowCount(EnumObjective enumObjective) {
        ArrayList<EnumScore> scores = objToScores.get(this.enumObjective);
        Integer count = lineCountScores.get(this.enumObjective);

        if (enumObjective != EnumObjective.CONSTANTS) {

            if (scores != null) {
                scores.add(this);
            } else {
                scores = new ArrayList<>();
                scores.add(this);
                objToScores.put(this.enumObjective, scores);
            }
        }

        if (count == null) {
            count = 1;
            lineCountScores.put(this.enumObjective, count);
        } else {
            count = this.isOneLiner ? count + 1 : count + 2;
            lineCountScores.replace(this.enumObjective, count);
        }
        if (count > maxLineCount) {
            throw new MaxScoreboardLineCountExceeded();
        }

    }


    public String getScoreText() {
        return scoreTextColor + scoreText;
    }

    private static void changeScoreValue(EnumScore enumScore,Player player, String value){
        AllianceScoreboardManager.getInstance().getAOBjectiveByName(player.getName()).changeScoreValue(enumScore,value);
    }



}
