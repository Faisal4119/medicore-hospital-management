# MediCore - Hospital Management System

CSE215L Project - Group 1
Faisal Bin Zaman (1813227042)

MediCore is a Java based Hospital Management System with a JavaFX GUI. It manages
patients, doctors, staff, appointments, medical records (prescriptions and lab reports)
and billing. All data is saved in CSV files inside the `data` folder so it stays
after closing the program (the files open directly in Excel). Passwords are stored
as ASCII codes, not plain text.

## OOP Concepts Used

- **Inheritance**: `Patient`, `Doctor`, `Staff` extend the abstract `Person` class.
  `Prescription` and `LabReport` extend the abstract `MedicalRecord` class.
- **Interfaces**: `Billable` (calculateBill) and `Schedulable` (book/cancel/reschedule).
- **Polymorphism**: `getSummary()` behaves differently for prescriptions vs lab reports.
- **Custom Exceptions**: `InvalidPatientIDException` and `AppointmentConflictException`.
- **Encapsulation**: private fields with getters and setters everywhere.

## Requirements

- JDK 17 or newer (tested with JDK 26)
- JavaFX SDK (tested with 25.0.1) - download from https://gluonhq.com/products/javafx/
  and unzip it somewhere (e.g. `C:\Users\YourName\javafx-sdk-25.0.1`)

## How to Run (Command Line)

1. Open `compile.bat` and `run.bat` in a text editor and make sure `PATH_TO_FX`
   points to the `lib` folder of your JavaFX SDK.
2. Double click `compile.bat` (or run it in a terminal).
3. Double click `run.bat`.

## How to Run (IntelliJ IDEA)

1. Open the project folder in IntelliJ.
2. Mark `src` as Sources Root (right click on src > Mark Directory as > Sources Root).
3. Go to File > Project Structure > Libraries > + > Java, and select the `lib`
   folder of the JavaFX SDK.
4. Run `main.Main` once (it will fail), then edit the Run Configuration and add
   this to VM options:
   ```
   --module-path "C:\Users\YourName\javafx-sdk-25.0.1\lib" --add-modules javafx.controls
   ```
   (If you don't see VM options: Modify options > Add VM options)
5. Run `main.Main` again.

## How to Use

1. First register a user account, then login with it.
2. From the dashboard you can:
   - **Manage Patients**: register new patients, view and update them.
   - **Appointments**: book an appointment with a doctor (4 doctors are added
     automatically on first run). Booking the same doctor or patient in the same
     slot shows a conflict error.
   - **Medical Records**: add prescriptions and lab reports for a patient and
     browse them.
   - **Billing**: see the unpaid bills of a patient and mark them as paid.
   - **Doctor List**: see all doctors with their department.
   - **Staff List**: see all staff members with ID, name, phone and role. Staff data
     comes from `data/staff.csv` (edit that file to change the staff).

## Packages

| Package    | Classes |
|------------|---------|
| main       | Main |
| model      | Person, Patient, Doctor, Staff, Appointment, AppointmentStatus, MedicalRecord, Prescription, LabReport |
| interfaces | Billable, Schedulable |
| storage    | Authentication, HospitalManager, FileManager |
| exceptions | InvalidPatientIDException, AppointmentConflictException |
| gui        | LoginView, DashboardView, PatientView, AppointmentView, RecordView, BillingView, DoctorView, StaffView |
