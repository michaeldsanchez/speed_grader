package edu.canyons.cs.speedgrader;
import cs.canyons.cs.speedgrader.util.UnzipWizard;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;


public class SpeedGrader {
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
        numCLAChoiceBox.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);  // options 0-9 for num CLA
        numIterChoiceBox.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);  // options 1-10 for num iterations

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

    @FXML
    private void handleExecuteButton(ActionEvent e) {
        // TODO: check that input & output file paths are selected
        // TODO: check that output path does not contain '.txt'
        // TODO: iterate over student folders to find java source
        // TODO: generate output text file using program outputs

        File classFolder= new File(inputPathTextField.getText());
        String outputDirName = outputPathTextField.getText();
        File workingFolder;

        // might only need a string for project main, append to input path
        File projectMainFile = new File(projectMainTextField.getText() + ".java");
        // only need a string for the output filename, append to output path
        File outputFile = new File(outputFilenameTextField.getText() + ".txt");

        if(unzipCheckBox.isSelected()) {
            // user specifies that unzipping is required for given class folder
            UnzipWizard.unzipDir(classFolder, outputDirName);
            workingFolder = new File(outputDirName);
        } else
            workingFolder = classFolder;

        File[] studentProjArray = workingFolder.listFiles();

        for(File eachProj: studentProjArray) {
            if(eachProj.isFile() && (eachProj.getName().endsWith(".java"))) {
                // execute each java source file and record output
                System.out.println("executing programs...");
            }
        } // end enumeration of class Folder
    }
}
