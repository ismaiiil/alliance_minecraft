package com.ismaiiil.alliance.Utils.Scoreboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;

public class AllianceObjective{

    public Objective theObjective;

    public AllianceObjective(Objective theObjective){
        this.theObjective = theObjective;
    }

}
