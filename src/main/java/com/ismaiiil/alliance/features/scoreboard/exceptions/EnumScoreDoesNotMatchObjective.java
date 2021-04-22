package com.ismaiiil.alliance.features.scoreboard.exceptions;

import com.ismaiiil.alliance.features.scoreboard.EnumObjective;
import com.ismaiiil.alliance.features.scoreboard.EnumScore;

public class EnumScoreDoesNotMatchObjective extends Exception {
    public EnumScoreDoesNotMatchObjective(EnumScore enumScore, EnumObjective enumObjective) {
        super("EnumObjective specified in enumScore " + enumScore.getEnumObjective() + " doest not match EnumObjective " + enumObjective);
    }
}
