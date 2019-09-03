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

    // for storing CLA inputs and program exec outputs for each individual iteration
    Controller[] controllersArray;
    StringBuilder masterLogOutput;

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

    private String runtimeProcess(String commandStr) {
        Process runtimeProcess = null;
        BufferedReader runtimeInputStream = null;
        BufferedReader runtimeErrorStream = null;
        StringBuilder runtimeOutput = new StringBuilder();

        try {
            runtimeProcess = Runtime.getRuntime().exec(commandStr);

            try {
                runtimeProcess.waitFor(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                runtimeInputStream = new BufferedReader(new InputStreamReader(runtimeProcess.getInputStream()));
                runtimeErrorStream = new BufferedReader(new InputStreamReader(runtimeProcess.getErrorStream()));

                String inputLine = runtimeInputStream.readLine();
                while (inputLine != null) {
                    runtimeOutput.append(inputLine);
                    inputLine = runtimeInputStream.readLine();
                }

                String errorLine = runtimeErrorStream.readLine();
                while (errorLine != null) {
                    runtimeOutput.append(errorLine + "\n");
                    errorLine = runtimeErrorStream.readLine();
                }

                runtimeProcess.destroy();

            } catch (IOException e) {
                System.err.println("Error on: readline()");
                e.printStackTrace();
            }

        } catch (IOException e) {
            // java object code failed to run
            System.err.println("Error while running: " + commandStr );
            e.printStackTrace();
        }

        return runtimeOutput.toString();
    } // end runtimeProcess(String):String

    private String compileJava(String absBuildXmlPath) {
        // returns true if the compile process was successful
        // TODO: test a failed compile
        // TODO: modularize process functions

//        Process compileProcess = null;
//        BufferedReader compileInputStream = null;
//        BufferedReader compileErrorStream = null;
//        StringBuilder compileOutput = new StringBuilder();

        String commandStr;
        // for attempting to find ant path with program
//            System.out.println("PATH:" + System.getenv("ANT_HOME"));
//            commandStr = System.getenv("ANT_HOME") + "/bin/ant -f " + absBuildXmlPath ;

        // for using a hard-coded ant path
        // TODO: make a config file to take this path and read it here
//        File file = new File("");
        commandStr = "/Applications/NetBeans/NetBeans8.2.app/Contents/Resources/NetBeans/extide/ant/bin/ant -f " + absBuildXmlPath;
        System.out.println(commandStr);

        return runtimeProcess(commandStr);
    } // end compileJava(String):boolean

    private String execJava(String absJarPath, String args) {
        // TODO: modularize process functions
        String projectMainFilename = projectMainTextField.getText();

//        Process execProcess = null;
//        BufferedReader execStream = null;
//        StringBuilder execOutput = new StringBuilder();

        String commandStr = "java -jar " + absJarPath + " " + args;

        if (userInputCheckBox.isSelected()) {
            try {
                BufferedWriter inputWriter = new BufferedWriter(new FileWriter(absJarPath.replace(projectMainFilename, "input.txt")));
                inputWriter.write(args.replace(" ", "\n"));

                commandStr = "java -jar " + absJarPath + " < input.txt";
            } catch (IOException e) {
                System.out.println("Error while writing to: input.txt");
                e.printStackTrace();
            }
        }

        System.out.println(commandStr);

//        try {
//            execProcess = Runtime.getRuntime().exec(commandStr);
//
//            try {
//                // TODO: input redirection with java to take user input
//                execStream = new BufferedReader(new InputStreamReader(execProcess.getInputStream()));
//
//                String currentLine = execStream.readLine();
//                while (currentLine != null) {
//                    execOutput.append(currentLine);
//                    currentLine = execStream.readLine();
//                }
//
//                execProcess.destroy();
//
//            } catch (IOException e) {
//                System.err.println("Error on: readline()");
//                e.printStackTrace();
//            }
//        } catch (IOException e) {
//            // java object code failed to run
//            System.err.println("Error while running: " + commandStr );
//            e.printStackTrace();
//        }

        return runtimeProcess(commandStr);
    } // end execJava(String, String):String

    @FXML
    private void handleExecuteButton(ActionEvent e) {
        // TODO: check that input & output file paths are selected
        // TODO: check that output path does not contain '.txt'
        // TODO: generate output text file using program outputs
        // stamp the top of the output txt file with the current date
        LocalDate localDate = LocalDate.now();
        masterLogOutput.append("DATE GRADED: " + localDate.toString());

        masterLogOutput.append("");

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
        String outputFilename = outputFilenameTextField.getText();
        BufferedWriter outputWriter;

        try {
            outputWriter = new BufferedWriter(new FileWriter(outputFilename));
            outputWriter.write(masterLogOutput.toString());

        } catch (IOException e) {
            System.out.println("Error while writing to: " + outputFilename);
            e.printStackTrace();

            // writing outputs failed
            return false;
        }

        // writing outputs succeeded
        return true;
    }
}
