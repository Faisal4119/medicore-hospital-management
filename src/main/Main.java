package main;

import javafx.application.Application;
import javafx.stage.Stage;
import gui.LoginView;
import storage.FileManager;
import storage.HospitalManager;

// main class that starts the whole program
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // load everything from the csv files first
        FileManager.loadAll();
        // add default doctors if this is the first run
        HospitalManager.seedDoctors();
        LoginView.show(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
