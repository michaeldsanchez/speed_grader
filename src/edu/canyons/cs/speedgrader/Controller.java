package edu.canyons.cs.speedgrader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;

public class Controller {
    // stores test inputs for each iteration required
    private static TextField[] textFieldsArray = new TextField[0];
    private FlowPane iterPane;
    private int currentIter;

    Controller(int numArgs, int iterNum) {
        // for every iteration, create a text field array with the number of args
        textFieldsArray = new TextField[numArgs];
        iterPane = new FlowPane();
        Region lb = new Region();
        currentIter = iterNum;

        iterPane.getChildren().add(new Label("Iteration " + iterNum + "\n"));

        if(numArgs > 0) {
            // if there is input required for testing

            for (int i = 0; i < numArgs; i++) {
                // init new text fields and add panes to GUI
                textFieldsArray[i] = new TextField();

                iterPane.getChildren().add(new Label("\ninput " + i + ":"));
                iterPane.getChildren().add(textFieldsArray[i]);
                // TODO: fix formatting of the input fields
            } // end text field generation for-loop
        } // end if statement for when input fields are needed
        else {
            iterPane.getChildren().add(new Label("NO INPUTS REQUIRED"));
        } // end else statement for no input fields needed
    } // end Controller(int, int):constructor

    public FlowPane getIterPane() {
        return iterPane;
    }

    public static String getArgs() {
        // for concatenating the input fields into a single args string
        StringBuilder args = new StringBuilder();

        for(TextField eachField: textFieldsArray) {
            args.append(eachField.getText()).append(" ");
        } // end args concatenation for-loop
        return args.toString();
    } // end getArgs():String

    public int getIterNum() {
        return currentIter;
    } // getCurrentIter():int
} // end Controller class
