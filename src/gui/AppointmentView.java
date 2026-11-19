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
import model.Appointment;
import model.AppointmentStatus;
import model.Doctor;
import model.Patient;
import storage.HospitalManager;
import exceptions.AppointmentConflictException;
import exceptions.InvalidPatientIDException;
import java.util.ArrayList;

// screen for booking, viewing and cancelling appointments
public class AppointmentView {

    public static void show(Stage stage, String username) {
        Label title = new Label("Appointments");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // combo boxes with "id - name" strings so its easy to pick
        ComboBox<String> patientBox = new ComboBox<String>();
        ArrayList<Patient> patients = HospitalManager.getPatients();
        for (int i = 0; i < patients.size(); i++) {
            patientBox.getItems().add(patients.get(i).getId() + " - " + patients.get(i).getName());
        }

        ComboBox<String> doctorBox = new ComboBox<String>();
        ArrayList<Doctor> doctors = HospitalManager.getDoctors();
        for (int i = 0; i < doctors.size(); i++) {
            Doctor d = doctors.get(i);
            doctorBox.getItems().add(d.getId() + " - " + d.getName() + " (" + d.getSpecialization() + ", " + d.getConsultationFee() + " Tk)");
        }

        DatePicker datePicker = new DatePicker();
        ComboBox<String> timeBox = new ComboBox<String>();
        timeBox.getItems().addAll("09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM", "03:00 PM", "04:00 PM");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.add(new Label("Patient:"), 0, 0);
        form.add(patientBox, 1, 0);
        form.add(new Label("Doctor:"), 0, 1);
        form.add(doctorBox, 1, 1);
        form.add(new Label("Date:"), 0, 2);
        form.add(datePicker, 1, 2);
        form.add(new Label("Time:"), 0, 3);
        form.add(timeBox, 1, 3);

        // table of all appointments
        TableView<Appointment> table = new TableView<Appointment>();
        TableColumn<Appointment, String> idCol = new TableColumn<Appointment, String>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<Appointment, String>("appointmentId"));
        TableColumn<Appointment, String> pCol = new TableColumn<Appointment, String>("Patient");
        pCol.setCellValueFactory(new PropertyValueFactory<Appointment, String>("patientId"));
        TableColumn<Appointment, String> dCol = new TableColumn<Appointment, String>("Doctor");
        dCol.setCellValueFactory(new PropertyValueFactory<Appointment, String>("doctorId"));
        TableColumn<Appointment, String> slotCol = new TableColumn<Appointment, String>("Time Slot");
        slotCol.setCellValueFactory(new PropertyValueFactory<Appointment, String>("timeSlot"));
        slotCol.setPrefWidth(160);
        TableColumn<Appointment, AppointmentStatus> statusCol = new TableColumn<Appointment, AppointmentStatus>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<Appointment, AppointmentStatus>("status"));
        TableColumn<Appointment, Double> feeCol = new TableColumn<Appointment, Double>("Fee");
        feeCol.setCellValueFactory(new PropertyValueFactory<Appointment, Double>("fee"));
        table.getColumns().add(idCol);
        table.getColumns().add(pCol);
        table.getColumns().add(dCol);
        table.getColumns().add(slotCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(feeCol);
        table.setItems(FXCollections.observableArrayList(HospitalManager.getAppointments()));
        table.setPrefHeight(220);

        Button bookBtn = new Button("Book Appointment");
        Button rescheduleBtn = new Button("Reschedule Selected");
        Button cancelBtn = new Button("Cancel Selected");
        Button backBtn = new Button("Back");

        bookBtn.setOnAction(e -> {
            if (patientBox.getValue() == null || doctorBox.getValue() == null
                    || datePicker.getValue() == null || timeBox.getValue() == null) {
                showError("Please select patient, doctor, date and time!");
                return;
            }
            // the id is the part before " - "
            String patientId = patientBox.getValue().split(" - ")[0];
            String doctorId = doctorBox.getValue().split(" - ")[0];
            String timeSlot = datePicker.getValue().toString() + " " + timeBox.getValue();
            try {
                Appointment appt = HospitalManager.bookAppointment(patientId, doctorId, timeSlot);
                table.setItems(FXCollections.observableArrayList(HospitalManager.getAppointments()));
                showInfo("Appointment booked! ID: " + appt.getAppointmentId());
            } catch (InvalidPatientIDException ex) {
                showError(ex.getMessage());
            } catch (AppointmentConflictException ex) {
                showError(ex.getMessage());
            }
        });

        // select an appointment in the table, pick a new date and time, then press this
        rescheduleBtn.setOnAction(e -> {
            Appointment selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select an appointment from the table first!");
                return;
            }
            if (selected.getStatus() == AppointmentStatus.CANCELLED) {
                showError("Cannot reschedule a cancelled appointment! Book a new one instead.");
                return;
            }
            if (datePicker.getValue() == null || timeBox.getValue() == null) {
                showError("Pick the new date and time first!");
                return;
            }
            String newSlot = datePicker.getValue().toString() + " " + timeBox.getValue();
            if (newSlot.equals(selected.getTimeSlot())) {
                showError("The appointment is already at that time!");
                return;
            }
            try {
                HospitalManager.rescheduleAppointment(selected, newSlot);
                table.refresh();
                showInfo("Appointment " + selected.getAppointmentId() + " moved to " + newSlot);
            } catch (InvalidPatientIDException ex) {
                showError(ex.getMessage());
            } catch (AppointmentConflictException ex) {
                showError(ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> {
            Appointment selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select an appointment from the table first!");
                return;
            }
            if (selected.getStatus() == AppointmentStatus.CANCELLED) {
                showError("This appointment is already cancelled!");
                return;
            }
            HospitalManager.cancelAppointment(selected);
            table.refresh();
            showInfo("Appointment " + selected.getAppointmentId() + " cancelled.");
        });

        backBtn.setOnAction(e -> DashboardView.show(stage, username));

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(bookBtn, rescheduleBtn, cancelBtn, backBtn);

        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(title, form, buttons, new Label("All Appointments:"), table);

        Scene scene = new Scene(root, 700, 560);
        stage.setScene(scene);
        stage.setTitle("MediCore - Appointments");
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
