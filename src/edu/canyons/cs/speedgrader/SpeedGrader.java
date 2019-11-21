/*
 ********************************************************************************
 *Project.............................................................SpeedGrader
 *Developer....................................................Michael D. Sanchez
 *Date last modified...................................................11/19/2019
 *Last modified by.............................................Michael D. Sanchez
 *
 *Description:   This file is for handling all of the GUI and main functional
 *               elements for the SpeedGrader project, which takes given inputs
 *               and executes students projects with those inputs for the number
 *               of iterations specified. All output is logged to a text file
 *               which can be found in the class project directory chosen.
 *
 *               This program utilizes Java's Runtime library to create a runtime
 *               process for which to compile student java projects with, using
 *               Ant to build Netbeans projects, and then executes the Jar files
 *               using a bash script for Unix/Linux, and a batch file for Windows.
 *********************************************************************************
 */
package edu.canyons.cs.speedgrader;

import edu.canyons.cs.speedgrader.util.UnzipWizard;
import edu.canyons.cs.speedgrader.util.Configuration;
import edu.canyons.cs.speedgrader.util.OutputLogger;

// imports for the GUI elements, using JavaFX and FXML
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

//  imports for file manipulations and reading process streams
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

// apache dependency for finding a file in a given directory
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class SpeedGrader {
    // FXML variables for GUI layout and backend functionality
    @FXML private ChoiceBox<Integer> numArgsChoiceBox;
    @FXML private ChoiceBox<Integer> numIterChoiceBox;
    @FXML private VBox IterVBox;
    @FXML private Button classProjDirButton;
    @FXML private Button antHomeDirButton;
    @FXML private TextField classProjDirTextField;
    @FXML private CheckBox unzipCheckBox;
    @FXML private CheckBox UnixLinuxCheckBox;
    @FXML private TextField outputFilenameTextField;
    @FXML private TextField antDirTextField;

    Controller[] controllersArray; // for storing program exec inputs for each individual iteration
    OutputLogger outputLog; // for logging all program output and generating


    public void initialize() {
        // initialize the choice boxes with very basic values, Command Line Args:0-20 and Iterations:1-10
        numArgsChoiceBox.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);  // options 0-9 for num CLA
        numIterChoiceBox.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);  // options 1-10 for num iterations

        // set default values for choice boxes
        numArgsChoiceBox.getSelectionModel().selectFirst();
        numIterChoiceBox.getSelectionModel().selectFirst();

        String antPath = Configuration.getConfigProp("ant");
        if (antPath != null) {
            antDirTextField.setText(antPath);
        } // end antPath init config
    } // end initialize:void

    @FXML
    private void handleGenerateButton(ActionEvent e) {
        int numIter = numIterChoiceBox.getSelectionModel().getSelectedItem();
        int numArgs = numArgsChoiceBox.getSelectionModel().getSelectedItem();

        // for cleaning up the iterations panel after each button press
        if (controllersArray != null)
            IterVBox.getChildren().clear();

        // init the array to store Controllers for the number of iterations needed
        controllersArray = new Controller[numIter];

        for (int i = 0; i < numIter; i++) {
            // append the needed panes for the input fields GUI
            controllersArray[i] = new Controller(numArgs, i);
            IterVBox.getChildren().add(controllersArray[i].getIterPane());

            // for verifying that testing inputs was received
            System.out.println(controllersArray[i].getArgs());
        } // end Controller factory for-loop
    } // end handleGenerateButton(ActionEvent):void

    @FXML
    private void handleInputDirButton(ActionEvent e) {
        handleDirChooser(classProjDirButton, classProjDirTextField);
    } // end handleInputPathButton(ActionEvent):void

    @FXML
    private void handleAntDirButton(ActionEvent e) {
        handleDirChooser(antHomeDirButton, antDirTextField);
    } // end handleOutputButton(ActionEvent):void

    private void handleDirChooser(Button chooserButton, TextField pathTextField) {
        DirectoryChooser path = new DirectoryChooser();
        File selectedDirectory = path.showDialog(chooserButton.getScene().getWindow());

        pathTextField.setText((selectedDirectory == null) ? "Please Select a Directory" : selectedDirectory.getAbsolutePath());
    } // end handleDirChooser

    private String runtimeProcess(List<String> commandArr) {
        // handles the runtime processes of the given command string
        // used for compiling and executing student projects using CLI
        Process runtimeProcess = null;

        // for reading and storing the output of the given command
        BufferedReader runtimeInputStream = null;
        BufferedReader runtimeErrorStream = null;
        StringBuilder runtimeOutput = new StringBuilder();

        try {
            String[] commandArray = new String[commandArr.size()];
            int i = 0;
            for (String cmdStr : commandArr) {
                commandArray[i] = cmdStr;
                i += 1;
            }

            runtimeProcess = Runtime.getRuntime().exec(commandArray);

            try {
                // mostly for compiling, give time for process to finish
                runtimeProcess.waitFor(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Error, process was interrupted: " + commandArr.toString());
                e.printStackTrace();
            } // end try-catch for process timeout

            try {
                // for testing
                System.out.println("Executing: " + commandArr.toString());

                // declare and init buffered readers to read command outputs and errors from the runtime process
                runtimeInputStream = new BufferedReader(new InputStreamReader(runtimeProcess.getInputStream()));
                runtimeErrorStream = new BufferedReader(new InputStreamReader(runtimeProcess.getErrorStream()));

                String inputLine = runtimeInputStream.readLine();
                while (inputLine != null) {
                    runtimeOutput.append(inputLine + "\n");
                    inputLine = runtimeInputStream.readLine();
                } // end while for reading and storing output

                String errorLine = runtimeErrorStream.readLine();
                while (errorLine != null) {
                    runtimeOutput.append(errorLine + "\n");
                    errorLine = runtimeErrorStream.readLine();
                } // end while for reading and storing output

                // end the process
                runtimeProcess.destroy();

            } catch (IOException e) {
                System.err.println("Error while reading outputs and errors");
                e.printStackTrace();
            } // end try-catch for reading outputs and errors

        } catch (IOException e) {
            System.err.println("Error while running: " + commandArr);
            e.printStackTrace();
        } // end try-catch for trying to run the given command

        return runtimeOutput.toString();
    } // end runtimeProcess(String):String

    private void antBuildXML(File classProjDir, File antDir) {
        // use apache commons library to find all of the build.xml files in the working directory of student projects
        Collection studentFiles = FileUtils.listFiles(classProjDir, new NameFileFilter("build.xml"), TrueFileFilter.TRUE);
        for (Object studentXmlFile : studentFiles) {
            File buildXmlFile = (File) studentXmlFile;

            // execute the given compile command using java runtime process
            if (UnixLinuxCheckBox.isSelected())
                System.out.println(runtimeProcess(new ArrayList<String>(Arrays.asList(antDir.getAbsolutePath(), "-f", buildXmlFile.getAbsolutePath()))));
            else
                System.out.println(runtimeProcess(new ArrayList<String>(Arrays.asList("cmd.exe", "/C", "\"" + antDir.getAbsolutePath() + "\"", "-f", buildXmlFile.getAbsolutePath()))));
        } // end compile files for-loop
    }

    private void execStudentJar(File classProjDir) {
        // now find all of the student jar files in the same class projects directory
        Collection studentJars = FileUtils.listFiles(classProjDir, new WildcardFileFilter("*.jar"), TrueFileFilter.TRUE);
        // generate a string containing all of the jar files to generate script list
        String jarFileList = "";
        ArrayList<String> commandStrArr;

        for (Object jarFile : studentJars) {
            if (jarFile == null)
                continue;

            File studentJarFile = (File) jarFile;
            String studentJarPath = studentJarFile.getAbsolutePath();
            System.out.println("absPath: " + studentJarPath);
            studentJarPath = studentJarPath.replaceAll("\\s+", "");
            System.out.println("fixedPath: " + studentJarPath);
            runtimeProcess(new ArrayList<String>(Arrays.asList("mv", studentJarPath, studentJarPath)));
            jarFileList += studentJarPath + " ";

            outputLog.appendStudentLog(studentJarPath);
        } // end .jar files for-loop

        String execString;
            for (Controller eachIter : controllersArray) {
                // for every iteration, get the arguments and generate shell script
                //            try {
                //                absInputTxtPath = classProjDirTextField.getText() + File.separator + ".sh";
                //                BufferedWriter inputWriter = new BufferedWriter(new FileWriter(absInputScriptPath));
                // reformat cla string so that the input arguments are newline separated instead
                //                inputWriter.write(Controller.getArgs());
                //                inputWriter.close();
                //            } catch (IOException r) {
                //                System.out.println("Error while writing to: input.txt");
                //                r.printStackTrace();
                //            } // end try-catch for writing user input to input.txt file


                // TODO: this is a mess btw
                String loopBody;
                // write a bash or batch script, for-loop variable is "item" - iter num is auto assigned
                loopBody = (UnixLinuxCheckBox.isSelected()) ? "echo $item\nsleep 1\n" : "ECHO %%item\nTIMEOUT 1 /NOBREAK\n";
                String inputScript = outputLog.writeLoopScript(Controller.getArgs(), "auto-input-iter", eachIter.getIterNum(), UnixLinuxCheckBox.isSelected(), loopBody);
                // modify user privileges on the shell script
                runtimeProcess(new ArrayList<String>(Arrays.asList("chmod", "777", inputScript)));

                loopBody = (UnixLinuxCheckBox.isSelected() ? "bash " + inputScript + " | java -jar $item\n" : inputScript + " | java -jar %%item\n");
                String execScript = outputLog.writeLoopScript(jarFileList, "auto-exec-iter", eachIter.getIterNum(), UnixLinuxCheckBox.isSelected(), loopBody);
                // modify user privileges on the shell script
                runtimeProcess(new ArrayList<String>(Arrays.asList("chmod", "777", execScript)));


                String runtimeOutput = runtimeProcess((UnixLinuxCheckBox.isSelected()) ? new ArrayList<>(Arrays.asList("bash", execScript)) : new ArrayList<>(Arrays.asList(execScript)));

                // for testing
                System.out.println("runtimeOutput: " + runtimeOutput);
                outputLog.appendIterOutputLog(eachIter.getIterNum(), runtimeOutput);
            } // end iterations for-loop
    }
    @FXML
    private void handleExecuteButton(ActionEvent e) {
        // TODO: check that output path does not contain '.txt'
        // TODO: generate output text file using program outputs
        // create an output log for all program execution outputs, including auto script generation
        outputLog = new OutputLogger(classProjDirTextField.getText(), outputFilenameTextField.getText());

        // do not run execution logic if any of the textfields are missing
        boolean cancelExecution = false;
        if (classProjDirTextField.getText().isEmpty() || classProjDirTextField.getText().equals("Please Select a Directory")) {
            classProjDirTextField.setText("Please enter the class project directory");
            cancelExecution = true;
        }
        // TODO:
        if (outputFilenameTextField.getText().isEmpty() || outputFilenameTextField.getText().equals("Please Select a Directory")) {
            outputFilenameTextField.setText("Please enter the output filename");
            cancelExecution = true;
        }
        if (antDirTextField.getText().isEmpty() || antDirTextField.getText().equals("Please Select a Directory")) {
            antDirTextField.setText("Please enter the ant path");
            cancelExecution = true;
        }
        if (cancelExecution)
            return;

        // TODO: not sure about this procedural logic atm...
        String antPath = antDirTextField.getText();
        Configuration.setConfigProp("ant", antPath);
        // get the absolute ant path from the config.properties
        String absAntPath = Configuration.getConfigProp("ant");
        File antDir = new File(absAntPath);

        // the folder which contains all of the java student projects
        File classProjDir = new File(classProjDirTextField.getText());

        if (unzipCheckBox.isSelected()) {
            // user specifies that unzipping is required for given class folder
            UnzipWizard.unzipDir(classProjDir); // output unzipped contents to class projects directory
        } // end if statement for unzip option

        // compile and execute the student projects
        antBuildXML(classProjDir, antDir);
        execStudentJar(classProjDir);

        // TODO: use checking to verify that output file is correctly saved and outputs logged
        boolean isLogSuccess = outputLog.writeLogOutputs();
    } // end handleExecuteButton(ActionEvent):void
}
