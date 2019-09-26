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
import java.util.Properties;
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
//    @FXML private Button outputDirButton;
//    @FXML private TextField outputDirTextField;
    @FXML private TextField outputFilenameTextField;
    @FXML private TextField antPathTextField;
//    @FXML private TextField projectMainTextField;

    // for storing CLA inputs and program exec outputs for each individual iteration
    Controller[] controllersArray;
    File[] studentXMLFiles;
    File[] studentJarFiles;
    StringBuilder masterLogOutput = new StringBuilder();
    String absInputTxtPath; // for when input redirection is used
    String absAntPath; // for compiling netbeans projects
    Properties prop = new Properties(); // for saving configuration

    public void initialize() {
        // initialize the choice boxes with very basic values, Command Line Args:0-9 and Iterations:1-10
        numCLAChoiceBox.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);  // options 0-9 for num CLA
        numIterChoiceBox.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);  // options 1-10 for num iterations

        // set default values for choice boxes
        numCLAChoiceBox.getSelectionModel().selectFirst();
        numIterChoiceBox.getSelectionModel().selectFirst();

        String antPath = getConfigProp("ant");
        if (antPath != null) {
            antPathTextField.setText(antPath);
        }
    }

    @FXML
    private void handleGenerateButton(ActionEvent e) {
        int numIter = numIterChoiceBox.getSelectionModel().getSelectedItem();
        int numCLA = numCLAChoiceBox.getSelectionModel().getSelectedItem();

        // for cleaning up the iterations panel after each button press
        if(controllersArray != null)
            claIterVBox.getChildren().clear();

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

//    @FXML private void handleOutputPathButton(ActionEvent e) {
//        DirectoryChooser outputPath = new DirectoryChooser();
//        File selectedDirectory = outputPath.showDialog(outputDirButton.getScene().getWindow());

//        outputDirTextField.setText((selectedDirectory == null) ? "Please Select a Directory": selectedDirectory.getAbsolutePath());
//    } // end handleOutputPathButton(ActionEvent):void

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
                // for testing
                System.out.print("reading runtime process input/error streams...");

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
            System.err.println("Error while running: " + commandStr );
            e.printStackTrace();
        } // end try-catch for trying to run the given command

        return runtimeOutput.toString();
    } // end runtimeProcess(String):String

    private String execJava(String absJarPath, String args) {
        String commandStr = "java -jar " + absJarPath + " " + args;

        if (userInputCheckBox.isSelected()) {
            // instead of executing the program with CLA, use input redirection
            commandStr = "java -jar " + absJarPath + " < " + absInputTxtPath;
        } // end if statement for test input type

        System.out.println("Executing:" + commandStr);

        return "COMMAND: " + commandStr + "\n\n" + runtimeProcess(commandStr);
    } // end execJava(String, String):String

    @FXML
    private void handleExecuteButton(ActionEvent e) {
        // TODO: check that output path does not contain '.txt'
        // TODO: generate output text file using program outputs
        // stamp the top of the output txt file with the current date
        LocalDate localDate = LocalDate.now();
        String dateGradedStr = "DATE GRADED: " + localDate.toString() + "\n\n";
        masterLogOutput.append(dateGradedStr);

        // do not run execution logic if any of the textfields are missing
        boolean cancelExecution = false;
        if (classProjDirTextField.getText().isEmpty() || classProjDirTextField.getText().equals("Please enter the class project directory")) {
            classProjDirTextField.setText("Please enter the class project directory");
            cancelExecution = true;
        }
        if (outputFilenameTextField.getText().isEmpty() || outputFilenameTextField.getText().equals("Please enter the output filename")) {
            outputFilenameTextField.setText("Please enter the output filename");
            cancelExecution = true;
        }
        if (antPathTextField.getText().isEmpty() || antPathTextField.getText().equals("Please enter the ant path")) {
            antPathTextField.setText("Please enter the ant path");
            cancelExecution = true;
        }
        if (cancelExecution)
            return;

        // TODO: wrap this into another function which lies in a popup config menu?
        String antPath = antPathTextField.getText();
        setConfigProp("ant", antPath);

        // the folder which contains all of the java student projects
        File classProjDir = new File(classProjDirTextField.getText());

        if (unzipCheckBox.isSelected()) {
            // user specifies that unzipping is required for given class folder
            UnzipWizard.unzipDir(classProjDir); // output unzipped contents to class projects directory
        } // end if statement for unzip option

        if (userInputCheckBox.isSelected()) {
            try {
                absInputTxtPath = classProjDirTextField.getText() + File.separator + "input.txt";
                System.out.println("writing input to: " + absInputTxtPath + ".txt");
                BufferedWriter inputWriter = new BufferedWriter(new FileWriter(absInputTxtPath));

                // reformat cla string so that the input arguments are newline separated instead
                inputWriter.write(Controller.getArgs().replace(" ", "\n"));
                inputWriter.close();
            } catch (IOException r) {
                System.out.println("Error while writing to: input.txt");
                r.printStackTrace();
            } // end try-catch for writing user input to input.txt file
        } // end if statement for test input type

        // use apache commons library to find all of the build.xml files in the working directory of student projects
        Collection studentFiles = FileUtils.listFiles(classProjDir, new NameFileFilter("build.xml"), TrueFileFilter.TRUE);
        String commandStr; // the command that will be executed by the java runtime process

        for (Object studentXmlFile : studentFiles) {
            File buildXmlFile = (File) studentXmlFile;

            // get the absolute ant path from the config.properties
            String absAntPath = getConfigProp("ant");

            // TODO: read this path to ant from config file
            commandStr = absAntPath + " -f " + buildXmlFile.getAbsolutePath();
            System.out.println("Compiling: " + commandStr);

            // execute the given command using java runtime process
            System.out.println(runtimeProcess(commandStr));
        } // end build.xml files for-loop

        for (Object jarFile : studentFiles) {
            if (jarFile == null)
                continue;

            File studentJarFile = (File) jarFile;
            String studentJarPath = studentJarFile.getAbsolutePath().replace("build.xml", "dist/*.jar");

            for (Controller eachIter: controllersArray) {
                String outputLog = execJava(studentJarPath, eachIter.getArgs());

                appendStudentLog(studentJarPath);
                appendIterOutputLog(eachIter.getIterNum(), outputLog);
            } // end iterations for-loop
        } // end .jar files for-loop

        // TODO: use checking to verify that output file is correctly saved and outputs logged
        boolean isLogSuccess = writeLogOutputs();
    } // end handleExecuteButton(ActionEvent):void

    public String getConfigProp(String propName) {
        // load the configuration from the properties file
        try (InputStream input = new FileInputStream("config.properties")) {
            prop.load(input); // load the properties file

            return prop.getProperty(propName);
        }
        catch (IOException e) {
            System.err.println("ERROR: while reading config.properties file");
            e.printStackTrace();
        }
        return null;
    } // end getConfigProp(String):String

    public void setConfigProp(String propName, String propValue) {
        // load the configuration from the properties file
        try (OutputStream output = new FileOutputStream("config.properties")) {
            prop.setProperty(propName, propValue); // set the properties value

            prop.store(output, null);
        }
        catch (IOException e) {
            System.err.println("ERROR: while writing config.properties file");
            e.printStackTrace();
        }
    } // end setConfigProp(String):void

    public void appendStudentLog(String student) {
        masterLogOutput.append("\nFILE: " + student);
    }

    public void appendIterOutputLog(int iter, String output) {
        masterLogOutput.append("\n------ ITERATION " + iter + " ------\n");
        masterLogOutput.append("\n** OUTPUT **\n" + output);
    }

    public boolean writeLogOutputs() {
        // use the output filename that the user entered to write final output log
        String outputFile = classProjDirTextField.getText() + File.separator + outputFilenameTextField.getText() + ".txt";

        // for testing
        System.out.println("output file written to:" + outputFile);

        BufferedWriter outputWriter;

        try {
            outputWriter = new BufferedWriter(new FileWriter(outputFile));
            outputWriter.write(masterLogOutput.toString());

            outputWriter.close();
        } catch (IOException e) {
            System.out.println("Error while writing to: " + outputFile);
            e.printStackTrace();

            // writing outputs failed
            return false;
        } // end try-catch for writing final output log

        // writing outputs succeeded
        return true;
    }
}
