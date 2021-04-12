package com.ismaiiil.alliance.Scoreboard.Exceptions;

import com.ismaiiil.alliance.Scoreboard.EnumObjective;

import static com.ismaiiil.alliance.Scoreboard.EnumScoreConstants.maxLineCount;


public class MaxScoreboardLineCountExceeded extends Exception{
    public MaxScoreboardLineCountExceeded(EnumObjective enumObjective) {
        super("Please check if " + enumObjective + " doesn't exceed " + maxLineCount);
    }
}
