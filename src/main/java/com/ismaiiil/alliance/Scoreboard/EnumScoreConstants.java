package com.ismaiiil.alliance.Scoreboard;

import java.util.LinkedHashMap;

public class EnumScoreConstants {

    public static int maxLineCount = 15;

    public static String SPACER_TEXT = "------------";
    public static String OUTLINE_TEXT = "                    ";
    public static String DEFAULT_DELIMITER = ": ";
    public static String ZERO = "Zero";
    public static String NONE = "None";

    public static LinkedHashMap<EnumObjective, EnumScore[]> allScores = new LinkedHashMap<>();
    public static LinkedHashMap<EnumObjective, Integer> allScoresCount = new LinkedHashMap<>();

}
