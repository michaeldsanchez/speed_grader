package edu.canyons.cs.speedgrader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.w3c.dom.Text;

public class Controller {
    private TextField[] textFieldsArray;
    private FlowPane iterPane;

    Controller(int numCLA, int currentIter) {
        textFieldsArray = new TextField[numCLA];
        iterPane = new FlowPane();

        iterPane.getChildren().add(new Label("Iteration" + currentIter + "\n"));

        if(numCLA > 0) {
            // if there is input required for testing

            for (int i = 0; i < numCLA; i++) {
                textFieldsArray[i] = new TextField();

                iterPane.getChildren().add(new Label("input " + i + ":"));
                iterPane.getChildren().add(textFieldsArray[i]);
                iterPane.getChildren().add(new Label("\n"));
            }
        }
        else {
            iterPane.getChildren().add(new Label("NO INPUTS REQUIRED"));
        }
    }

    public FlowPane getIterPane() {
        return iterPane;
    }

    public String getArgs() {
        String args = "";
        for(TextField eachField: textFieldsArray) {
            args += eachField.getText() + " ";
        }
        return args;
    }
}
