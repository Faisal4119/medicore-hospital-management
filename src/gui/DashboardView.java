package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// main menu after login, access point for all the features
public class DashboardView {

    public static void show(Stage stage, String username) {
        Label title = new Label("MediCore Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label welcome = new Label("Welcome, " + username + "!");

        Button patientBtn = new Button("Manage Patients");
        Button appointmentBtn = new Button("Appointments");
        Button recordBtn = new Button("Medical Records");
        Button billingBtn = new Button("Billing");
        Button doctorBtn = new Button("Doctor List");
        Button staffBtn = new Button("Staff List");
        Button logoutBtn = new Button("Logout");

        patientBtn.setPrefWidth(200);
        appointmentBtn.setPrefWidth(200);
        recordBtn.setPrefWidth(200);
        billingBtn.setPrefWidth(200);
        doctorBtn.setPrefWidth(200);
        staffBtn.setPrefWidth(200);
        logoutBtn.setPrefWidth(200);

        patientBtn.setOnAction(e -> PatientView.show(stage, username));
        appointmentBtn.setOnAction(e -> AppointmentView.show(stage, username));
        recordBtn.setOnAction(e -> RecordView.show(stage, username));
        billingBtn.setOnAction(e -> BillingView.show(stage, username));
        doctorBtn.setOnAction(e -> DoctorView.show(stage, username));
        staffBtn.setOnAction(e -> StaffView.show(stage, username));
        logoutBtn.setOnAction(e -> LoginView.show(stage));

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(title, welcome, patientBtn, appointmentBtn, recordBtn, billingBtn, doctorBtn, staffBtn, logoutBtn);

        Scene scene = new Scene(root, 500, 550);
        stage.setScene(scene);
        stage.setTitle("MediCore - Dashboard");
    }
}
