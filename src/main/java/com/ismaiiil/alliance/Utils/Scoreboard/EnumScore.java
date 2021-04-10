package com.ismaiiil.alliance.Utils.Scoreboard;

import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.Utils.ConfigLoading.JSON.PlayerData;
import com.ismaiiil.alliance.Utils.Scoreboard.Exceptions.EnumScoreDoesNotMatchObjective;
import com.ismaiiil.alliance.Utils.Scoreboard.Exceptions.MaxScoreboardLineCountExceeded;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.security.SecureRandom;
import java.util.*;

import static com.ismaiiil.alliance.Utils.Scoreboard.EnumScoreConstants.*;

@Getter
public enum EnumScore { //Note: order of enum affects the order of the scoreboard

    //------------------------------------------------------------------------------------------------------------------
    //Constant objective scores (USED FOR SPACING)
    //------------------------------------------------------------------------------------------------------------------
    CONST_SPACER(EnumObjective.CONSTS,
            SPACER_TEXT,
            ChatColor.YELLOW.toString(),
            "",
            "",
            "",
            true) { public void updateLinkedValue(Player player) {}},

    CONST_OUTLINE(EnumObjective.CONSTS,
            OUTLINE_TEXT,
            ChatColor.GREEN.toString() + ChatColor.STRIKETHROUGH.toString(),
            "",
            "",
            "",
            true) { public void updateLinkedValue(Player player) {}},

    //------------------------------------------------------------------------------------------------------------------
    //balance objective scores
    //------------------------------------------------------------------------------------------------------------------


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


    }

    ;


    static {
        allScores.put( EnumObjective.BALANCE ,
                    new EnumScore[]{
                            CONST_OUTLINE,
                            BALANCE_CURRENT,
                            CONST_SPACER,
                            BALANCE_USED,
                            CONST_SPACER,
                            BALANCE_CLAIMS,
                            CONST_OUTLINE
                    });
        allScores.put( EnumObjective.WAR ,
                    new EnumScore[]{
                            CONST_OUTLINE,
                            WAR_CURRENT,
                            CONST_OUTLINE
                    });

        for (Map.Entry<EnumObjective, EnumScore[]> entry: allScores.entrySet()) {
            int totalRows = 0;
            for (EnumScore enumScore: entry.getValue()) {
                if (enumScore.isOneLiner){
                    totalRows ++;
                }else{
                    totalRows += 2;
                }

                if (enumScore.getEnumObjective() != entry.getKey()){
                    try {
                        throw new EnumScoreDoesNotMatchObjective(enumScore, entry.getKey());
                    } catch (EnumScoreDoesNotMatchObjective e) {
                        e.printStackTrace();
                    }
                }

            }
            allScoresCount.put(entry.getKey(), totalRows);
            if (totalRows > maxLineCount){
                try {
                    throw new MaxScoreboardLineCountExceeded(entry.getKey());
                } catch (MaxScoreboardLineCountExceeded e) {
                    e.printStackTrace();
                }
            }


        }

    }


    public abstract void updateLinkedValue(Player player);

    private final EnumObjective enumObjective;
    private final String scoreText;
    private final String scoreTextColor;
    private String defaultScoreValue;
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
    }

    public String getScoreText() {
        return scoreTextColor + scoreText;
    }

    private static void changeScoreValue(EnumScore enumScore,Player player, String value){
        AllianceScoreboardManager.getInstance().getAOBjectiveByName(player.getName()).changeScoreValue(enumScore,value);
    }



}
