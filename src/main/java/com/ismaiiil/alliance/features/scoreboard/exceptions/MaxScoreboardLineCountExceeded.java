package com.ismaiiil.alliance.features.scoreboard.exceptions;

import com.ismaiiil.alliance.features.scoreboard.EnumObjective;

import static com.ismaiiil.alliance.features.scoreboard.EnumScoreConstants.maxLineCount;


public class MaxScoreboardLineCountExceeded extends Exception{
    public MaxScoreboardLineCountExceeded(EnumObjective enumObjective) {
        super("Please check if " + enumObjective + " doesn't exceed " + maxLineCount);
    }
}
