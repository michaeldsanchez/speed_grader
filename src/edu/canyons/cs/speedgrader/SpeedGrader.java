package edu.canyons.cs.speedgrader;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import edu.canyons.cs.speedgrader.util.UnzipWizard;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class SpeedGrader {
    // FXML variables for gui layout
    @FXML private ChoiceBox<Integer> numCLAChoiceBox;
    @FXML private ChoiceBox<Integer> numIterChoiceBox;
    @FXML private Button generateButton;
    @FXML private VBox claIterVBox;
    @FXML private Button inputPathButton;
    @FXML private TextField inputPathTextField;
    @FXML private CheckBox unzipCheckBox;
    @FXML private Button outputPathButton;
    @FXML private TextField outputPathTextField;
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
        File selectedDirectory = inputPath.showDialog(inputPathButton.getScene().getWindow());

        inputPathTextField.setText(selectedDirectory.getAbsolutePath());
    }

    @FXML
    private void handleOutputPathButton(ActionEvent e) {
        DirectoryChooser outputPath = new DirectoryChooser();
        File selectedDirectory = outputPath.showDialog(outputPathButton.getScene().getWindow());

        outputPathTextField.setText(selectedDirectory.getAbsolutePath());
    }

    private static String compileJava(String absSourcePath) {
        // returns true if the compile process was successful
        // TODO: test a failed compile
        Process compileProcess = null;
        BufferedReader compileInputStream = null;
        BufferedReader compileErrorStream = null;
        StringBuilder compileOutput = new StringBuilder();

        try {
            compileProcess = Runtime.getRuntime().exec("javac " + absSourcePath);

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


            } catch (IOException e) {
                System.err.println("Error on: execStream.readline()");
                e.printStackTrace();
            }
        } catch (IOException e) {
            // java source code failed to compile
            System.out.println("failed to compile " + absSourcePath);
        }

        return compileOutput.toString();
    } // end compileJava(String):boolean

    private static String execJava(String absSourcePath, String args) {
        String absObjectPath = absSourcePath.replaceAll(".java", "");
        Process execProcess = null;
        BufferedReader execStream = null;
        StringBuilder execOutput = new StringBuilder();

        try {
            execProcess = Runtime.getRuntime().exec("java " + absObjectPath + " " + args);

            try {
                execStream = new BufferedReader(new InputStreamReader(execProcess.getInputStream()));

                String currentLine = execStream.readLine();
                while (currentLine != null) {
                    execOutput.append(currentLine);
                    currentLine = execStream.readLine();
                }
            } catch (IOException e) {
                System.err.println("Error on: execStream.readline()");
                e.printStackTrace();
            }
        } catch (IOException e) {
            // java object code failed to run
            System.err.println("Error while running: javac " + absObjectPath);
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
        File classFolder= new File(inputPathTextField.getText());

        // might only need a string for project main, append to input path
        String projectMainFilename = projectMainTextField.getText() + ".java";

        if(unzipCheckBox.isSelected()) {
            // user specifies that unzipping is required for given class folder
            UnzipWizard.unzipDir(classFolder, outputFilenameTextField.getText());

            // output unzipped contents to specified output directory, becomes new working directory
            classFolder = new File(outputPathTextField.getText());
        }

        // use apache commons library to find a file in our working directory of student projects
        Collection studentFiles = FileUtils.listFiles(classFolder, new String[] {"java"}, true);

        for (Iterator iterator = studentFiles.iterator(); iterator.hasNext();) {
            File javaFile = (File)iterator.next();

            if (javaFile.getName().compareTo(projectMainFilename) == 0) {
                System.out.println("Execute: " + javaFile.getAbsolutePath());
                System.out.println(compileJava(javaFile.getAbsolutePath()));
            }
        }
    }
}
