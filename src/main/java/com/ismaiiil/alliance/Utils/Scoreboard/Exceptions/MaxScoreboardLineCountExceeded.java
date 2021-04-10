package com.ismaiiil.alliance.Utils.Scoreboard.Exceptions;

import com.ismaiiil.alliance.Utils.Scoreboard.EnumObjective;
import com.ismaiiil.alliance.Utils.Scoreboard.EnumScore;

import static com.ismaiiil.alliance.Utils.Scoreboard.EnumScoreConstants.maxLineCount;


public class MaxScoreboardLineCountExceeded extends Exception{
    public MaxScoreboardLineCountExceeded(EnumObjective enumObjective) {
        super("Please check if " + enumObjective + " doesn't exceed " + maxLineCount);
    }
}
