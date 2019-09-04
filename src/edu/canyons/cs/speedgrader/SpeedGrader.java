package edu.canyons.cs.speedgrader;
import edu.canyons.cs.speedgrader.util.UnzipWizard;

// imports for the GUI elements, using JavaFX and FXML
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

//  imports for file manipulations and reading process streams
import java.io.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

// apache dependency for finding a file in a given directory
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class SpeedGrader {
    // FXML variables for GUI layout and backend functionality
    @FXML private ChoiceBox<Integer> numCLAChoiceBox;
    @FXML private ChoiceBox<Integer> numIterChoiceBox;
    @FXML private CheckBox userInputCheckBox;
    @FXML private VBox claIterVBox;
    @FXML private Button classProjDirButton;
    @FXML private TextField classProjDirTextField;
    @FXML private CheckBox unzipCheckBox;
    @FXML private Button outputDirButton;
    @FXML private TextField outputDirTextField;
    @FXML private TextField outputFilenameTextField;
    @FXML private TextField projectMainTextField;

    // for storing CLA inputs and program exec outputs for each individual iteration
    Controller[] controllersArray;
    StringBuilder masterLogOutput = new StringBuilder();

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

        // init the array to store Controllers for the number of iterations needed
        controllersArray = new Controller[numIter];

        for (int i = 0; i < numIter; i++) {
            // append the needed panes for the input fields GUI
            controllersArray[i] = new Controller(numCLA, i);
            claIterVBox.getChildren().add(controllersArray[i].getIterPane());

            // for verifying that testing inputs was received
            System.out.println(controllersArray[i].getArgs());
        } // end Controller factory for-loop

    } // end handleGenerateButton(ActionEvent):void

    @FXML private void handleInputPathButton(ActionEvent e) {
        DirectoryChooser inputPath = new DirectoryChooser();
        File selectedDirectory = inputPath.showDialog(classProjDirButton.getScene().getWindow());

        classProjDirTextField.setText((selectedDirectory == null) ? "Please Select a Directory": selectedDirectory.getAbsolutePath());
    } // end handleInputPathButton(ActionEvent):void

    @FXML private void handleOutputPathButton(ActionEvent e) {
        DirectoryChooser outputPath = new DirectoryChooser();
        File selectedDirectory = outputPath.showDialog(outputDirButton.getScene().getWindow());

        outputDirTextField.setText((selectedDirectory == null) ? "Please Select a Directory": selectedDirectory.getAbsolutePath());
    } // end handleOutputPathButton(ActionEvent):void

    private String runtimeProcess(String commandStr) {
        // handles the runtime processes of the given command string
        // used for compiling and executing student projects using CLI
        Process runtimeProcess = null;

        // for reading and storing the output of the given command
        BufferedReader runtimeInputStream = null;
        BufferedReader runtimeErrorStream = null;
        StringBuilder runtimeOutput = new StringBuilder();

        try {
            runtimeProcess = Runtime.getRuntime().exec(commandStr);

            try {
                // mostly for compiling, give time for process to finish
                runtimeProcess.waitFor(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Error, process was interrupted: " + commandStr);
                e.printStackTrace();
            } // end try-catch for process timeout

            try {
                // declare and init buffered readers to read command outputs and errors from the runtime process
                runtimeInputStream = new BufferedReader(new InputStreamReader(runtimeProcess.getInputStream()));
                runtimeErrorStream = new BufferedReader(new InputStreamReader(runtimeProcess.getErrorStream()));

                String inputLine = runtimeInputStream.readLine();
                while (inputLine != null) {
                    runtimeOutput.append(inputLine);
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
            System.err.println("Error while running: " + commandStr );
            e.printStackTrace();
        } // end try-catch for trying to run the given command

        return runtimeOutput.toString();
    } // end runtimeProcess(String):String

    private String compileJava(String absBuildXmlPath) {
        // TODO: this function can be eliminated?

        String commandStr;
        // TODO: make a config file to take this path and read it here
        commandStr = "/Applications/NetBeans/NetBeans8.2.app/Contents/Resources/NetBeans/extide/ant/bin/ant -f " + absBuildXmlPath;
        System.out.println(commandStr);

        return runtimeProcess(commandStr);
    } // end compileJava(String):String

    private String execJava(String absJarPath, String args) {
        String projectMainFilename = projectMainTextField.getText();
        String commandStr = "java -jar " + absJarPath + " " + args;

        if (userInputCheckBox.isSelected()) {
            // instead of executing the program with CLA, use input redirection
            try {
                // TODO: test with using an abs path to a global input.txt file
                String absInputTxtPath = classProjDirTextField.getText() + File.separator + "input.txt";
                System.out.println("writing to: " + absInputTxtPath);
                BufferedWriter inputWriter = new BufferedWriter(new FileWriter(absInputTxtPath));

                inputWriter.write(args.replace(" ", "\n"));
                inputWriter.close();

                commandStr = "java -jar " + absJarPath + " < " + absInputTxtPath;

            } catch (IOException e) {
                System.out.println("Error while writing to: input.txt");
                e.printStackTrace();
            } // end try-catch for writing user input to input.txt file
        } // end if statement for test input type

        System.out.println(commandStr);

        return runtimeProcess(commandStr);
    } // end execJava(String, String):String

    @FXML
    private void handleExecuteButton(ActionEvent e) {
        // TODO: check that output path does not contain '.txt'
        // TODO: generate output text file using program outputs
        // stamp the top of the output txt file with the current date
        LocalDate localDate = LocalDate.now();
        masterLogOutput.append("DATE GRADED: " + localDate.toString());

        masterLogOutput.append("");

        // the folder which contains all of the java student projects
        File classProjDir = new File(classProjDirTextField.getText());

        // the name of the project which will be used when executing each student's .jar file
        String projectMainFilename = projectMainTextField.getText() + ".jar";

        if (unzipCheckBox.isSelected()) {
            // user specifies that unzipping is required for given class folder
            UnzipWizard.unzipDir(classProjDir, outputDirTextField.getText());

            // output unzipped contents to specified output directory, becomes new working directory
            classProjDir = new File(outputDirTextField.getText());
        } // end if statement for unzip option

        // use apache commons library to find all of the build.xml files in the working directory of student projects
        Collection studentXmlFiles = FileUtils.listFiles(classProjDir, new NameFileFilter("build.xml"), TrueFileFilter.TRUE);
        for (Object studentXmlFile : studentXmlFiles) {
            File buildXmlFile = (File) studentXmlFile;

            System.out.println("Compiling: " + buildXmlFile.getAbsolutePath());
            System.out.println(compileJava(buildXmlFile.getAbsolutePath()));
        } // end build.xml files for-loop

        // use apache commons library to find all of the .jar files in the working directory of student projects
        Collection studentJarFiles = FileUtils.listFiles(classProjDir, new String[]{"jar"}, true);
        for (Object jarFile : studentJarFiles) {
            File studentJarFile = (File) jarFile;

            // for testing that the correct project name is being used to execute
            System.out.println("exec file: " + studentJarFile.getAbsolutePath());

            if(studentJarFile.getName().compareTo(projectMainFilename) == 0) {
                for (Controller eachIter: controllersArray) {
                    System.out.println("Executing:" + studentJarFile.getAbsolutePath());
                    System.out.println(execJava(studentJarFile.getAbsolutePath(), eachIter.getArgs()));
                } // end iterations for-loop
            } // end if statement for executing correct .jar file
        } // end .jar files for-loop

        // TODO: use checking to verify that output file is correctly saved and outputs logged
        boolean isLogSuccess = writeLogOutputs();
    }

    public void appendLogOutput(String studentFile, int iterNum, String newOutput) {
        // TODO: call helper methods to concatenate newOutputs to masterLogOutput
        masterLogOutput.append("\nFILE: " + studentFile);
        masterLogOutput.append("\n------ ITERATION " + iterNum + " ------\n");
        masterLogOutput.append("\n** OUTPUT **\n" + newOutput);
    }

    public boolean writeLogOutputs() {
        // use the output filename that the user entered to write final output log
        String outputFilename = outputFilenameTextField.getText();
        BufferedWriter outputWriter;

        try {
            outputWriter = new BufferedWriter(new FileWriter(outputFilename));
            outputWriter.write(masterLogOutput.toString());

            outputWriter.close();
        } catch (IOException e) {
            System.out.println("Error while writing to: " + outputFilename);
            e.printStackTrace();

            // writing outputs failed
            return false;
        } // end try-catch for writing final output log

        // writing outputs succeeded
        return true;
    }
}
