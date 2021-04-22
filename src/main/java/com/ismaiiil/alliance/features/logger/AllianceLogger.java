package com.ismaiiil.alliance.features.logger;

import com.ismaiiil.alliance.AlliancePlugin;

import java.util.logging.Level;

public class AllianceLogger {

    public static void log(Level level, String Message){
        AlliancePlugin.getInstance().getLogger().log(Level.SEVERE, "[Alliance] " + Message);
    }
}
