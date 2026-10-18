package com.pation.awardnomination.nominee;

import java.time.LocalDateTime;


// DESIGN PATTERN: SINGLETON PATTERN

public class SystemLogger {


    private static SystemLogger instance;


    private SystemLogger() {
    }

    public static SystemLogger getInstance() {
        if (instance == null) {
            instance = new SystemLogger();
        }
        return instance;
    }


    public void logActivity(String action) {
        System.out.println("[SYSTEM LOG] - " + LocalDateTime.now() + " : " + action);
    }
}