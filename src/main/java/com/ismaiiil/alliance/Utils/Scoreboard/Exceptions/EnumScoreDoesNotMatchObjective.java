package com.ismaiiil.alliance.Utils.Scoreboard.Exceptions;

import com.ismaiiil.alliance.Utils.Scoreboard.EnumObjective;
import com.ismaiiil.alliance.Utils.Scoreboard.EnumScore;

import java.util.LinkedHashMap;

import static com.ismaiiil.alliance.Utils.Scoreboard.EnumScoreConstants.maxLineCount;

public class EnumScoreDoesNotMatchObjective extends Exception {
    public EnumScoreDoesNotMatchObjective(EnumScore enumScore, EnumObjective enumObjective) {
        super("EnumObjective specified in enumScore " + enumScore.getEnumObjective() + " doest not match EnumObjective " + enumObjective);
    }
}
