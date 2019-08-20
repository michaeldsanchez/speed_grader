package edu.canyons.cs.speedgrader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("speedgrader.fxml"));
        primaryStage.setTitle("Speed Grader");
        primaryStage.setScene(new Scene(root,640,460));
        primaryStage.show();
    }
}
