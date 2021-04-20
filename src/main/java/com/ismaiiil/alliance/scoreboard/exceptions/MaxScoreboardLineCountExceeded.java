package com.ismaiiil.alliance.scoreboard.exceptions;

import com.ismaiiil.alliance.scoreboard.EnumObjective;

import static com.ismaiiil.alliance.scoreboard.EnumScoreConstants.maxLineCount;


public class MaxScoreboardLineCountExceeded extends Exception{
    public MaxScoreboardLineCountExceeded(EnumObjective enumObjective) {
        super("Please check if " + enumObjective + " doesn't exceed " + maxLineCount);
    }
}
