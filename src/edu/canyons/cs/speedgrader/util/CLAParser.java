package edu.canyons.cs.speedgrader.util;

public class CLAParser {
    // TODO: add checking

    public static int getInt(String arg) {
        return Integer.parseInt(arg);
    } // end getInt(String):int

    public static double getDouble(String arg) {
        return Double.parseDouble(arg);
    } // end getDouble(String):int

    public static char getChar(String arg) {
        return arg.charAt(0);
    } // end getChar(String):char
}
