package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import storage.Authentication;

// the first screen with login, register and exit options
public class LoginView {

    public static void show(Stage stage) {
        Label title = new Label("MediCore - Hospital Management System");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");
        Button exitBtn = new Button("Exit");
        loginBtn.setPrefWidth(100);
        registerBtn.setPrefWidth(100);
        exitBtn.setPrefWidth(100);

        loginBtn.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();
            if (Authentication.login(username, password)) {
                DashboardView.show(stage, username);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Login Failed");
                alert.setContentText("Wrong username or password!");
                alert.showAndWait();
            }
        });

        registerBtn.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();
            String error = Authentication.register(username, password);
            if (error == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Registration Successful");
                alert.setContentText("You can now login with your new account.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Registration Failed");
                alert.setContentText(error);
                alert.showAndWait();
            }
        });

        exitBtn.setOnAction(e -> {
            stage.close();
        });

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(title, grid, loginBtn, registerBtn, exitBtn);

        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.setTitle("MediCore - Login");
        stage.show();
    }
}
