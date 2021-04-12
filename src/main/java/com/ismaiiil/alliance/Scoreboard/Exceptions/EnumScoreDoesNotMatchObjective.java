package com.ismaiiil.alliance.Scoreboard.Exceptions;

import com.ismaiiil.alliance.Scoreboard.EnumObjective;
import com.ismaiiil.alliance.Scoreboard.EnumScore;

public class EnumScoreDoesNotMatchObjective extends Exception {
    public EnumScoreDoesNotMatchObjective(EnumScore enumScore, EnumObjective enumObjective) {
        super("EnumObjective specified in enumScore " + enumScore.getEnumObjective() + " doest not match EnumObjective " + enumObjective);
    }
}
