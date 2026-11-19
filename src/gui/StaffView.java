package gui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Staff;
import storage.HospitalManager;

// screen that shows the list of all staff members
public class StaffView {

    public static void show(Stage stage, String username) {
        Label title = new Label("Staff List");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Staff> table = new TableView<Staff>();
        TableColumn<Staff, String> idCol = new TableColumn<Staff, String>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<Staff, String>("id"));
        TableColumn<Staff, String> nameCol = new TableColumn<Staff, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<Staff, String>("name"));
        nameCol.setPrefWidth(200);
        TableColumn<Staff, String> phoneCol = new TableColumn<Staff, String>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<Staff, String>("phone"));
        phoneCol.setPrefWidth(150);
        TableColumn<Staff, String> roleCol = new TableColumn<Staff, String>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<Staff, String>("role"));
        roleCol.setPrefWidth(130);
        table.getColumns().add(idCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(phoneCol);
        table.getColumns().add(roleCol);
        table.setItems(FXCollections.observableArrayList(HospitalManager.getStaffList()));
        table.setPrefHeight(300);

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> DashboardView.show(stage, username));

        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(title, table, backBtn);

        Scene scene = new Scene(root, 580, 420);
        stage.setScene(scene);
        stage.setTitle("MediCore - Staff");
    }
}
