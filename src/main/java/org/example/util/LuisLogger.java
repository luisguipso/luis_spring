package org.example.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LuisLogger {
    public static String GREEN = "\u001B[32m";
    public static String YELLOW = "\u001B[33m";
    public static String WHITE = "\u001B[37m";
    public static String RESET = "\u001B[0m";
    public static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(Class module, String message){
        String time = LocalDateTime.now().format(DATE_FORMAT);
        System.out.printf(GREEN+ "%15s " + YELLOW + "%-40s: " + WHITE + "%s\n" + RESET, time, getName(module), message);
    }

    public static void log(Class module, String message, Exception e){
        message += "%nCaused by: %s%n    %s";
        message = String.format(message, e, getExceptionStackTrace(e));
        log(module, message);
    }

    private static String getExceptionStackTrace(Exception e) {
        return Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n    "));
    }

    private static String getName(Class module) {
        String name = module.getName();
        if (name.length() <= 40)
            return name;

        return getShortName(name);
    }

    private static String getShortName(String name) {
        String[] elements = name.split("\\.");
        String packageName = getShortPackageName(elements);
        return packageName + elements[elements.length-1];
    }

    private static String getShortPackageName(String[] elements) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < elements.length - 1; i++)
            builder.append(elements[i].charAt(0)).append(".");
        return builder.toString();
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
