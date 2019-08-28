package edu.canyons.cs.speedgrader;
import edu.canyons.cs.speedgrader.util.UnzipWizard;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

// apache dependency for finding a file
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class SpeedGrader {
    // FXML variables for gui layout
    @FXML private ChoiceBox<Integer> numCLAChoiceBox;
    @FXML private ChoiceBox<Integer> numIterChoiceBox;
    @FXML private Button generateButton;
    @FXML private VBox claIterVBox;
    @FXML private Button classProjDirButton;
    @FXML private TextField classProjDirTextField;
    @FXML private CheckBox unzipCheckBox;
    @FXML private Button outputDirButton;
    @FXML private TextField outputDirTextField;
    @FXML private TextField outputFilenameTextField;
    @FXML private TextField projectMainTextField;
    @FXML private Button executeButton;

    Controller[] controllersArray;

    public void initialize() {
        // initialize the choice boxes with very basic values, Command Line Args:0-9 and Iterations:1-10
        numCLAChoiceBox.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);  // options 0-9 for num CLA
        numIterChoiceBox.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);  // options 1-10 for num iterations

        // set default values for choice boxes
        numCLAChoiceBox.getSelectionModel().selectFirst();
        numIterChoiceBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleGenerateButton(ActionEvent e) {
        int numIter = numIterChoiceBox.getSelectionModel().getSelectedItem();
        int numCLA = numCLAChoiceBox.getSelectionModel().getSelectedItem();

        controllersArray = new Controller[numIter];

        for (int i = 0; i < numIter; i++) {
            // TODO: add a pane to the claIterScrollPane
            controllersArray[i] = new Controller(numCLA, i);
            claIterVBox.getChildren().add(controllersArray[i].getIterPane());

            // for testing input
            System.out.println(controllersArray[i].getArgs());
        }
    }

    @FXML
    private void handleInputPathButton(ActionEvent e) {
        DirectoryChooser inputPath = new DirectoryChooser();
        File selectedDirectory = inputPath.showDialog(classProjDirButton.getScene().getWindow());

        classProjDirTextField.setText((selectedDirectory == null) ? "Please Select a Directory": selectedDirectory.getAbsolutePath());
    }

    @FXML
    private void handleOutputPathButton(ActionEvent e) {
        DirectoryChooser outputPath = new DirectoryChooser();
        File selectedDirectory = outputPath.showDialog(outputDirButton.getScene().getWindow());

        outputDirTextField.setText((selectedDirectory == null) ? "Please Select a Directory": selectedDirectory.getAbsolutePath());
    }

    private static String compileJava(String absBuildXmlPath) {
        // returns true if the compile process was successful
        // TODO: test a failed compile
        // TODO: modularize process functions

        Process compileProcess = null;
        BufferedReader compileInputStream = null;
        BufferedReader compileErrorStream = null;
        StringBuilder compileOutput = new StringBuilder();

        try {
            String commandStr;

            // for attempting to find ant path with program
//            System.out.println("PATH:" + System.getenv("ANT_HOME"));
//            commandStr = System.getenv("ANT_HOME") + "/bin/ant -f " + absBuildXmlPath ;

            // for using a hard-coded ant path
            // TODO: make a config file to take this path and read it here
            File file = new File("/Applications/NetBeans/NetBeans8.2.app/Contents/Resources/NetBeans/extide/ant/bin/ant");
            commandStr = file.getAbsolutePath() + " -f " + absBuildXmlPath;
            System.out.println(commandStr);
            compileProcess = Runtime.getRuntime().exec(commandStr);

            try {
                compileProcess.waitFor(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                compileInputStream = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
                compileErrorStream = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));

                String inputLine = compileInputStream.readLine();
                while (inputLine != null) {
                    compileOutput.append(inputLine + "\n");
                    inputLine = compileInputStream.readLine();
                }

                String errorLine = compileErrorStream.readLine();
                while (errorLine != null) {
                    compileOutput.append(errorLine + "\n");
                    errorLine = compileErrorStream.readLine();
                }

                compileProcess.destroy();

            } catch (IOException e) {
                System.err.println("Error on: execStream.readline()");
                e.printStackTrace();
            }
        } catch (IOException e) {
            // java source code failed to compile
            System.out.println("Error while running: ANT_HOME -f " + absBuildXmlPath);
            e.printStackTrace();
        }

        return compileOutput.toString();
    } // end compileJava(String):boolean

    private static String execJava(String projectMainFilename, String args) {
        // TODO: modularize process functions

        Process execProcess = null;
        BufferedReader execStream = null;
        StringBuilder execOutput = new StringBuilder();

        try {
            execProcess = Runtime.getRuntime().exec("java -jar " + projectMainFilename + " " + args);

            try {
                // TODO: input redirection with java to take user input
                execStream = new BufferedReader(new InputStreamReader(execProcess.getInputStream()));

                String currentLine = execStream.readLine();
                while (currentLine != null) {
                    execOutput.append(currentLine);
                    currentLine = execStream.readLine();
                }

                execProcess.destroy();

            } catch (IOException e) {
                System.err.println("Error on: execStream.readline()");
                e.printStackTrace();
            }
        } catch (IOException e) {
            // java object code failed to run
            System.err.println("Error while running: java -jar " + projectMainFilename);
            e.printStackTrace();
        }

        return execOutput.toString();
    } // end execJava(String, String):String

    @FXML
    private void handleExecuteButton(ActionEvent e) {
        // TODO: check that input & output file paths are selected
        // TODO: check that output path does not contain '.txt'
        // TODO: generate output text file using program outputs
        // the folder which contains all of the java student projects
        File classProjDir = new File(classProjDirTextField.getText());

        // might only need a string for project main, append to input path
        String projectMainFilename = projectMainTextField.getText() + ".jar";

        if (unzipCheckBox.isSelected()) {
            // user specifies that unzipping is required for given class folder
            UnzipWizard.unzipDir(classProjDir, outputDirTextField.getText());

            // output unzipped contents to specified output directory, becomes new working directory
            classProjDir = new File(outputDirTextField.getText());
        }

        // use apache commons library to find a file in our working directory of student projects
        Collection studentXmlFiles = FileUtils.listFiles(classProjDir, new NameFileFilter("build.xml"), TrueFileFilter.TRUE);
        for (Object studentXmlFile : studentXmlFiles) {
            File buildXmlFile = (File) studentXmlFile;

            System.out.println("Compiling: " + buildXmlFile.getAbsolutePath());
            System.out.println(compileJava(buildXmlFile.getAbsolutePath()));
        }

        Collection studentJarFiles = FileUtils.listFiles(classProjDir, new String[]{"jar"}, true);
        for (Object jarFile : studentJarFiles) {
            File studentJarFile = (File) jarFile;

            System.out.println("exec file: " + studentJarFile.getAbsolutePath());
            if(studentJarFile.getName().compareTo(projectMainFilename) == 0) {
                for (Controller eachIter: controllersArray) {
                    System.out.println("Executing:" + studentJarFile.getAbsolutePath());
                    System.out.println(execJava(studentJarFile.getAbsolutePath(), eachIter.getArgs()));
                }
            }
        }
    }
}
