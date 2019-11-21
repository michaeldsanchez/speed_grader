package edu.canyons.cs.speedgrader.util;

import java.io.*;
import java.util.Properties;

public class Configuration {
    static Properties prop = new Properties(); // for saving configuration

    public static String getConfigProp(String propName) {
        // load the configuration from the properties file
        try (InputStream input = new FileInputStream("config.properties")) {
            prop.load(input); // load the properties file

            return prop.getProperty(propName);
        } catch (IOException e) {
            System.err.println("ERROR: while reading config.properties file");
            e.printStackTrace();
        }
        return null;
    } // end getConfigProp(String):String

    public static void setConfigProp(String propName, String propValue) {
        // load the configuration from the properties file
        try (OutputStream output = new FileOutputStream("config.properties")) {
            prop.setProperty(propName, propValue); // set the properties value

            prop.store(output, null);
        } catch (IOException e) {
            System.err.println("ERROR: while writing config.properties file");
            e.printStackTrace();
        }
    } // end setConfigProp(String):void
}
