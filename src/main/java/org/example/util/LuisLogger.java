package org.example.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LuisLogger {
    public static String GREEN = "\u001B[32m";
    public static String YELLOW = "\u001B[33m";
    public static String WHITE = "\u001B[37m";
    public static String RESET = "\u001B[0m";
    public static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(Class module, String message){
        String time = LocalDateTime.now().format(DATE_FORMAT);
        System.out.printf(GREEN+ "%15s " + YELLOW + "%-30s: " + WHITE + "%s\n" + RESET, time, module.getName(), message);
    }

    public static void showBanner(){
        System.out.printf(YELLOW);
        String banner ="""
        ____       __          _      _____            _                ____  
        \\ \\ \\     / /   __  __(_)____/ ___/____  _____(_)___  ____ _    \\ \\ \\ 
         \\ \\ \\   / /   / / / / / ___/\\__ \\/ __ \\/ ___/ / __ \\/ __ `/     \\ \\ \\  LuisSpring Web Framework
         / / /  / /___/ /_/ / (__  )___/ / /_/ / /  / / / / / /_/ /      / / /  For Educational Purposes
        /_/_/  /_____/\\__,_/_/____//____/ .___/_/  /_/_/ /_/\\__, /      /_/_/   By Luis Gomes
                                       /_/                 /____/             """;
        System.out.println(banner);
        System.out.println(RESET);
    }
}
