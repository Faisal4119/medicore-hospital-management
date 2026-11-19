package gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.LabReport;
import model.MedicalRecord;
import model.Patient;
import model.Prescription;
import storage.HospitalManager;
import exceptions.InvalidPatientIDException;
import java.time.LocalDate;
import java.util.ArrayList;

// screen for browsing a patients prescriptions and lab reports, and adding new ones
public class RecordView {

    public static void show(Stage stage, String username) {
        Label title = new Label("Medical Records");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<String> patientBox = new ComboBox<String>();
        ArrayList<Patient> patients = HospitalManager.getPatients();
        for (int i = 0; i < patients.size(); i++) {
            patientBox.getItems().add(patients.get(i).getId() + " - " + patients.get(i).getName());
        }

        ListView<String> recordList = new ListView<String>();
        recordList.setPrefHeight(150);

        Button showBtn = new Button("Show Records");
        showBtn.setOnAction(e -> {
            if (patientBox.getValue() == null) {
                showError("Select a patient first!");
                return;
            }
            String patientId = patientBox.getValue().split(" - ")[0];
            recordList.getItems().clear();
            try {
                Patient p = HospitalManager.findPatient(patientId);
                ArrayList<MedicalRecord> records = p.getRecords();
                if (records.size() == 0) {
                    recordList.getItems().add("No records found for this patient.");
                }
                for (int i = 0; i < records.size(); i++) {
                    MedicalRecord r = records.get(i);
                    // polymorphism - getSummary() works differently for each record type
                    recordList.getItems().add("[" + r.getDate() + "] " + r.getSummary());
                }
            } catch (InvalidPatientIDException ex) {
                showError(ex.getMessage());
            }
        });

        HBox topRow = new HBox(10);
        topRow.getChildren().addAll(new Label("Patient:"), patientBox, showBtn);

        // --- add prescription section ---
        Label presLabel = new Label("Add Prescription:");
        presLabel.setStyle("-fx-font-weight: bold;");
        TextField medField = new TextField();
        TextField dosageField = new TextField();
        TextField durationField = new TextField();
        GridPane presForm = new GridPane();
        presForm.setHgap(10);
        presForm.setVgap(6);
        presForm.add(new Label("Medicine:"), 0, 0);
        presForm.add(medField, 1, 0);
        presForm.add(new Label("Dosage:"), 0, 1);
        presForm.add(dosageField, 1, 1);
        presForm.add(new Label("Duration:"), 0, 2);
        presForm.add(durationField, 1, 2);
        Button addPresBtn = new Button("Add Prescription");

        addPresBtn.setOnAction(e -> {
            if (patientBox.getValue() == null) {
                showError("Select a patient first!");
                return;
            }
            if (medField.getText().trim().equals("") || dosageField.getText().trim().equals("")
                    || durationField.getText().trim().equals("")) {
                showError("Please fill in medicine, dosage and duration!");
                return;
            }
            if (hasComma(medField.getText()) || hasComma(dosageField.getText()) || hasComma(durationField.getText())) {
                showError("Fields cannot contain commas!");
                return;
            }
            String patientId = patientBox.getValue().split(" - ")[0];
            Prescription pres = new Prescription(HospitalManager.nextRecordId(), LocalDate.now().toString(),
                    patientId, medField.getText().trim(), dosageField.getText().trim(), durationField.getText().trim());
            try {
                HospitalManager.addRecord(pres);
                medField.clear();
                dosageField.clear();
                durationField.clear();
                showInfo("Prescription added.");
            } catch (InvalidPatientIDException ex) {
                showError(ex.getMessage());
            }
        });

        // --- add lab report section ---
        Label labLabel = new Label("Add Lab Report:");
        labLabel.setStyle("-fx-font-weight: bold;");
        TextField testField = new TextField();
        TextField resultField = new TextField();
        TextField rangeField = new TextField();
        GridPane labForm = new GridPane();
        labForm.setHgap(10);
        labForm.setVgap(6);
        labForm.add(new Label("Test Name:"), 0, 0);
        labForm.add(testField, 1, 0);
        labForm.add(new Label("Result:"), 0, 1);
        labForm.add(resultField, 1, 1);
        labForm.add(new Label("Normal Range:"), 0, 2);
        labForm.add(rangeField, 1, 2);
        Button addLabBtn = new Button("Add Lab Report");

        addLabBtn.setOnAction(e -> {
            if (patientBox.getValue() == null) {
                showError("Select a patient first!");
                return;
            }
            if (testField.getText().trim().equals("") || resultField.getText().trim().equals("")
                    || rangeField.getText().trim().equals("")) {
                showError("Please fill in test name, result and range!");
                return;
            }
            if (hasComma(testField.getText()) || hasComma(resultField.getText()) || hasComma(rangeField.getText())) {
                showError("Fields cannot contain commas!");
                return;
            }
            String patientId = patientBox.getValue().split(" - ")[0];
            LabReport report = new LabReport(HospitalManager.nextRecordId(), LocalDate.now().toString(),
                    patientId, testField.getText().trim(), resultField.getText().trim(), rangeField.getText().trim());
            try {
                HospitalManager.addRecord(report);
                testField.clear();
                resultField.clear();
                rangeField.clear();
                showInfo("Lab report added.");
            } catch (InvalidPatientIDException ex) {
                showError(ex.getMessage());
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> DashboardView.show(stage, username));

        HBox forms = new HBox(30);
        VBox presBox = new VBox(8);
        presBox.getChildren().addAll(presLabel, presForm, addPresBtn);
        VBox labBox = new VBox(8);
        labBox.getChildren().addAll(labLabel, labForm, addLabBtn);
        forms.getChildren().addAll(presBox, labBox);

        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(title, topRow, recordList, forms, backBtn);

        Scene scene = new Scene(root, 700, 560);
        stage.setScene(scene);
        stage.setTitle("MediCore - Medical Records");
    }

    private static boolean hasComma(String s) {
        return s.contains(",");
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
