package gui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Doctor;
import storage.HospitalManager;

// screen that shows the list of all doctors with their department
public class DoctorView {

    public static void show(Stage stage, String username) {
        Label title = new Label("Doctor List");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Doctor> table = new TableView<Doctor>();
        TableColumn<Doctor, String> nameCol = new TableColumn<Doctor, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<Doctor, String>("name"));
        nameCol.setPrefWidth(200);
        TableColumn<Doctor, String> deptCol = new TableColumn<Doctor, String>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<Doctor, String>("specialization"));
        deptCol.setPrefWidth(200);
        table.getColumns().add(nameCol);
        table.getColumns().add(deptCol);
        table.setItems(FXCollections.observableArrayList(HospitalManager.getDoctors()));
        table.setPrefHeight(300);

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> DashboardView.show(stage, username));

        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(title, table, backBtn);

        Scene scene = new Scene(root, 500, 420);
        stage.setScene(scene);
        stage.setTitle("MediCore - Doctors");
    }
}
