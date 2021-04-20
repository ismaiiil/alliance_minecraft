package com.ismaiiil.alliance.scoreboard.exceptions;

import com.ismaiiil.alliance.scoreboard.EnumObjective;
import com.ismaiiil.alliance.scoreboard.EnumScore;

public class EnumScoreDoesNotMatchObjective extends Exception {
    public EnumScoreDoesNotMatchObjective(EnumScore enumScore, EnumObjective enumObjective) {
        super("EnumObjective specified in enumScore " + enumScore.getEnumObjective() + " doest not match EnumObjective " + enumObjective);
    }
}
