package gui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Appointment;
import model.AppointmentStatus;
import model.Patient;
import storage.FileManager;
import storage.HospitalManager;
import exceptions.InvalidPatientIDException;
import java.util.ArrayList;

// screen that shows itemized bills for a patient and lets us mark them as paid
public class BillingView {

    public static void show(Stage stage, String username) {
        Label title = new Label("Billing");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<String> patientBox = new ComboBox<String>();
        ArrayList<Patient> patients = HospitalManager.getPatients();
        for (int i = 0; i < patients.size(); i++) {
            patientBox.getItems().add(patients.get(i).getId() + " - " + patients.get(i).getName());
        }

        // table of unpaid appointments for the selected patient
        TableView<Appointment> table = new TableView<Appointment>();
        TableColumn<Appointment, String> idCol = new TableColumn<Appointment, String>("Appointment ID");
        idCol.setCellValueFactory(new PropertyValueFactory<Appointment, String>("appointmentId"));
        idCol.setPrefWidth(120);
        TableColumn<Appointment, String> dCol = new TableColumn<Appointment, String>("Doctor");
        dCol.setCellValueFactory(new PropertyValueFactory<Appointment, String>("doctorId"));
        TableColumn<Appointment, String> slotCol = new TableColumn<Appointment, String>("Time Slot");
        slotCol.setCellValueFactory(new PropertyValueFactory<Appointment, String>("timeSlot"));
        slotCol.setPrefWidth(160);
        TableColumn<Appointment, Double> feeCol = new TableColumn<Appointment, Double>("Fee (Tk)");
        feeCol.setCellValueFactory(new PropertyValueFactory<Appointment, Double>("fee"));
        table.getColumns().add(idCol);
        table.getColumns().add(dCol);
        table.getColumns().add(slotCol);
        table.getColumns().add(feeCol);
        table.setPrefHeight(220);

        Label totalLabel = new Label("Total Due: 0.0 Tk");
        totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button showBtn = new Button("Show Bill");
        Button payBtn = new Button("Mark Selected as Paid");
        Button backBtn = new Button("Back");

        showBtn.setOnAction(e -> {
            refreshTable(patientBox, table, totalLabel);
        });

        payBtn.setOnAction(e -> {
            Appointment selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select a bill from the table first!");
                return;
            }
            selected.setPaid(true);
            FileManager.saveAll();
            showInfo("Bill for " + selected.getAppointmentId() + " marked as paid.");
            refreshTable(patientBox, table, totalLabel);
        });

        backBtn.setOnAction(e -> DashboardView.show(stage, username));

        HBox topRow = new HBox(10);
        topRow.getChildren().addAll(new Label("Patient:"), patientBox, showBtn);

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(payBtn, backBtn);

        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(title, topRow, new Label("Unpaid Bills:"), table, totalLabel, buttons);

        Scene scene = new Scene(root, 650, 520);
        stage.setScene(scene);
        stage.setTitle("MediCore - Billing");
    }

    // reloads the unpaid appointments of the selected patient into the table
    private static void refreshTable(ComboBox<String> patientBox, TableView<Appointment> table, Label totalLabel) {
        if (patientBox.getValue() == null) {
            showError("Select a patient first!");
            return;
        }
        String patientId = patientBox.getValue().split(" - ")[0];
        try {
            Patient p = HospitalManager.findPatient(patientId);
            ArrayList<Appointment> unpaid = new ArrayList<Appointment>();
            ArrayList<Appointment> all = p.getAppointments();
            for (int i = 0; i < all.size(); i++) {
                Appointment a = all.get(i);
                if (!a.isPaid() && a.getStatus() != AppointmentStatus.CANCELLED) {
                    unpaid.add(a);
                }
            }
            table.setItems(FXCollections.observableArrayList(unpaid));
            // calculateBill() comes from the Billable interface
            totalLabel.setText("Total Due: " + p.calculateBill() + " Tk");
        } catch (InvalidPatientIDException ex) {
            showError(ex.getMessage());
        }
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
