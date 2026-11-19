package gui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Patient;
import storage.FileManager;
import storage.HospitalManager;

// screen for registering new patients and viewing/updating patient details
public class PatientView {

    public static void show(Stage stage, String username) {
        Label title = new Label("Patient Management");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // form for adding a new patient
        TextField nameField = new TextField();
        TextField ageField = new TextField();
        TextField phoneField = new TextField();
        TextField diseaseField = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.add(new Label("Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Age:"), 0, 1);
        form.add(ageField, 1, 1);
        form.add(new Label("Phone:"), 0, 2);
        form.add(phoneField, 1, 2);
        form.add(new Label("Disease:"), 0, 3);
        form.add(diseaseField, 1, 3);

        // table showing all patients
        TableView<Patient> table = new TableView<Patient>();
        TableColumn<Patient, String> idCol = new TableColumn<Patient, String>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("id"));
        TableColumn<Patient, String> nameCol = new TableColumn<Patient, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("name"));
        nameCol.setPrefWidth(120);
        TableColumn<Patient, Integer> ageCol = new TableColumn<Patient, Integer>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<Patient, Integer>("age"));
        TableColumn<Patient, String> phoneCol = new TableColumn<Patient, String>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("phone"));
        phoneCol.setPrefWidth(110);
        TableColumn<Patient, String> diseaseCol = new TableColumn<Patient, String>("Disease");
        diseaseCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("disease"));
        diseaseCol.setPrefWidth(120);
        table.getColumns().add(idCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(ageCol);
        table.getColumns().add(phoneCol);
        table.getColumns().add(diseaseCol);
        table.setItems(FXCollections.observableArrayList(HospitalManager.getPatients()));
        table.setPrefHeight(220);

        Button addBtn = new Button("Register Patient");
        Button loadBtn = new Button("Load Selected");
        Button updateBtn = new Button("Update Selected");
        Button backBtn = new Button("Back");

        addBtn.setOnAction(e -> {
            String error = checkInputs(nameField.getText(), ageField.getText(), phoneField.getText(), diseaseField.getText());
            if (error != null) {
                showError(error);
                return;
            }
            int age = Integer.parseInt(ageField.getText().trim());
            Patient p = new Patient(HospitalManager.nextPatientId(), nameField.getText().trim(), age,
                    phoneField.getText().trim(), diseaseField.getText().trim());
            HospitalManager.addPatient(p);
            table.setItems(FXCollections.observableArrayList(HospitalManager.getPatients()));
            nameField.clear();
            ageField.clear();
            phoneField.clear();
            diseaseField.clear();
            showInfo("Patient registered with ID: " + p.getId());
        });

        // fills the form with the selected patients data so we can edit it
        loadBtn.setOnAction(e -> {
            Patient selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select a patient from the table first!");
                return;
            }
            nameField.setText(selected.getName());
            ageField.setText("" + selected.getAge());
            phoneField.setText(selected.getPhone());
            diseaseField.setText(selected.getDisease());
        });

        updateBtn.setOnAction(e -> {
            Patient selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select a patient from the table first!");
                return;
            }
            String error = checkInputs(nameField.getText(), ageField.getText(), phoneField.getText(), diseaseField.getText());
            if (error != null) {
                showError(error);
                return;
            }
            selected.setName(nameField.getText().trim());
            selected.setAge(Integer.parseInt(ageField.getText().trim()));
            selected.setPhone(phoneField.getText().trim());
            selected.setDisease(diseaseField.getText().trim());
            FileManager.saveAll();
            table.refresh();
            showInfo("Patient " + selected.getId() + " updated.");
        });

        backBtn.setOnAction(e -> DashboardView.show(stage, username));

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(addBtn, loadBtn, updateBtn, backBtn);

        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(title, form, buttons, new Label("All Patients:"), table);

        Scene scene = new Scene(root, 650, 550);
        stage.setScene(scene);
        stage.setTitle("MediCore - Patients");
    }

    // simple validation for the form, returns error message or null if ok
    private static String checkInputs(String name, String age, String phone, String disease) {
        if (name.trim().equals("") || age.trim().equals("") || phone.trim().equals("") || disease.trim().equals("")) {
            return "Please fill in all the fields!";
        }
        if (name.contains(",") || phone.contains(",") || disease.contains(",")) {
            return "Fields cannot contain commas!";
        }
        try {
            int a = Integer.parseInt(age.trim());
            if (a <= 0 || a > 150) {
                return "Age must be between 1 and 150!";
            }
        } catch (NumberFormatException e) {
            return "Age must be a number!";
        }
        return null;
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
