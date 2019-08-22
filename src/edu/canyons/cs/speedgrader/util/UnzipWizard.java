/*
 ********************************************************************************
 *Project.............................................................UnzipWizard
 *Developer...................................................Daniel Garret Jaffe
 *Date last modified...................................................08/19/2019
 *Last modified by................................................Michael Sanchez
 *
 *Description:   This file is used to handle to work of extracting zipped files
 *               to a location. Utilizes opensource zip4j.jar to handle the
 *               actual unzip functionality. More information can be found at
 *               http://www.lingala.net/zip4j/
 *
 *               Unzips all zipped files in a given directory and then scans
 *               the directory of the unzipped files to find and unzip zipped
 *               files that were from the parent zip file.
 *               This file is the main interface the user will interact with.
 *               It uses the Unzipper.java class to handle the work of file
 *               extractions and uses UserInput to manage user input.
 ********************************************************************************
 */
package edu.canyons.cs.speedgrader.util;
import java.io.File;
import net.lingala.zip4j.core.ZipFile;

public class UnzipWizard {
    
    public static void unzipDir(File classFolder, String outputDirName) {
        // TODO: rename variables to make more modular! i.e. not specific to speed_grader
        // unzips an entire directory using zip4j from unzip function
        File zipDirs[] = classFolder.listFiles();

        for(File zipFile: zipDirs) {
            if(zipFile.isFile() && (zipFile.getName().endsWith(".zip"))) {
                // create a new directory for the unzip using original project name
                String fixedProjDir = zipFile.getName().replaceAll(".zip", "");
                String outputFilePath = outputDirName + File.separator + fixedProjDir;

                File outputDir = new File(outputFilePath);
                outputDir.mkdirs();

                unzipFile(zipFile.getPath(), outputDir.toString());
            } // end .zip finding logic
        } // end enumeration of class Folder
    } // end unzipDir(String): void

    public static void unzipFile(String filePath, String outputDirStr) {
        // utilizes the open source zip4j.jar to handle unzipping functionality
        try {
            ZipFile zipFile = new ZipFile(filePath);
            zipFile.extractAll(outputDirStr);
        } catch (Exception e) {
            System.out.println(e);
        } // end catch statement
    } // end unzipFile(String): void
} // end UnzipWizard class