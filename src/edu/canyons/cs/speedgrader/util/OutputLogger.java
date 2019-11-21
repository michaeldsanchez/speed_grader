package edu.canyons.cs.speedgrader.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class OutputLogger {
    StringBuilder masterLogOutput;
    String outputFile;
    String classProjDir;


    public OutputLogger(String classProjDir, String outputFilename) {
        // stamp the top of the output txt file with the current date
        this.masterLogOutput = new StringBuilder();
        this.classProjDir = classProjDir;
        this.outputFile = classProjDir + File.separator + outputFilename + ".txt";

        LocalDate localDate = LocalDate.now();
        String dateGradedStr = "DATE GRADED: " + localDate.toString() + "\n\n";
        masterLogOutput.append(dateGradedStr);
    } // end Constructor:OutputLog

    public void appendStudentLog(String student) {
        masterLogOutput.append("\nFILE: " + student);
    }

    public void appendIterOutputLog(int iter, String output) {
        masterLogOutput.append("\n------ ITERATION " + iter + " ------\n");
        masterLogOutput.append("\n** OUTPUT **\n" + output);
    }

    public boolean writeLogOutputs() {
        // use the output filename that the user entered to write final output log
        // for testing
        System.out.println("output file written to:" + outputFile);

        BufferedWriter outputWriter;
        try {
            outputWriter = new BufferedWriter(new FileWriter(outputFile));
            outputWriter.write(masterLogOutput.toString());

            outputWriter.close();
        } catch (IOException e) {
            System.err.println("Error while writing to: " + outputFile);
            e.printStackTrace();

            // writing outputs failed
            return false;
        } // end try-catch for writing final output log

        // writing outputs succeeded
        return true;
    }

    public String writeLoopScript(String javaFileList, String scriptName, int iterNum, Boolean isLinux, String loopBody) {
        // generates an os dependent script for executing student jar files
        // TODO: might need integrity checking at this point

        // PARAM: javaFileList is a space separated string of all java files
        // PARAM: iterArgs is the arguments of a specific iteration
        BufferedWriter scriptWriter;
        String filename = "";

        try {
            filename = classProjDir + File.separator + scriptName + iterNum + ((isLinux) ? ".sh" : ".bat");
            scriptWriter = new BufferedWriter(new FileWriter(filename));

            // shebang and initial script information
            if (isLinux) {
                scriptWriter.write("#!/bin/bash\n" +
                        "# this script was auto-generated using SpeedGrader\n" +
                        "list='" + javaFileList + "'\n" +
                        "for item in $list\n" +
                        "do\n" +
                        loopBody +
                        "echo finished $item\n" +
                        "done\n");
                scriptWriter.close();
            } else {
                scriptWriter.write("ECHO OFF\n" +
                        "REM this script was auto-generated using SpeedGrader\n" +
                        "SET list=" + javaFileList + "\n" +
                        "(for %%item in (%list%) do (\n" +
                        loopBody +
                        "echo finished %item\n" +
                        "))\n" +
                        "PAUSE\n");
                ;
                scriptWriter.close();
            }
        } catch (IOException e) {
            System.err.println("Error while writing exec script...");
            e.printStackTrace();
        }

        return filename;
    }
}
