package storage;

import model.*;
import java.io.*;
import java.util.ArrayList;

// reads and writes everything to csv files in the data folder
public class FileManager {

    private static final String DATA_FOLDER = "data";

    // makes sure the data folder exists
    private static void checkFolder() {
        File folder = new File(DATA_FOLDER);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    // reads all lines from a file, returns empty list if file doesnt exist yet
    private static ArrayList<String> readLines(String fileName) {
        ArrayList<String> lines = new ArrayList<String>();
        File file = new File(DATA_FOLDER + File.separator + fileName);
        if (!file.exists()) {
            return lines;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                if (!line.trim().equals("")) {
                    lines.add(line);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // writes all lines to a file (overwrites the old file)
    private static void writeLines(String fileName, ArrayList<String> lines) {
        checkFolder();
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(DATA_FOLDER + File.separator + fileName));
            for (int i = 0; i < lines.size(); i++) {
                writer.println(lines.get(i));
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------- loading ----------

    public static void loadAll() {
        checkFolder();
        loadUsers();
        Authentication.migrateOldPasswords();
        loadDoctors();
        loadPatients();
        loadStaff();
        loadAppointments();
        loadPrescriptions();
        loadLabReports();
    }

    private static void loadUsers() {
        ArrayList<String> lines = readLines("users.csv");
        for (int i = 0; i < lines.size(); i++) {
            Authentication.getUsers().add(lines.get(i));
        }
    }

    private static void loadDoctors() {
        ArrayList<String> lines = readLines("doctors.csv");
        for (int i = 0; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",");
            // id,name,age,phone,specialization,fee
            Doctor d = new Doctor(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4], Double.parseDouble(p[5]));
            HospitalManager.getDoctors().add(d);
        }
    }

    private static void loadPatients() {
        ArrayList<String> lines = readLines("patients.csv");
        for (int i = 0; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",");
            // id,name,age,phone,disease
            Patient patient = new Patient(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4]);
            HospitalManager.getPatients().add(patient);
        }
    }

    private static void loadStaff() {
        ArrayList<String> lines = readLines("staff.csv");
        for (int i = 0; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",");
            // id,name,age,phone,role,salary
            Staff s = new Staff(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4], Double.parseDouble(p[5]));
            HospitalManager.getStaffList().add(s);
        }
    }

    private static void loadAppointments() {
        ArrayList<String> lines = readLines("appointments.csv");
        for (int i = 0; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",");
            // id,patientId,doctorId,timeSlot,status,fee,paid
            Appointment a = new Appointment(p[0], p[1], p[2], p[3], Double.parseDouble(p[5]));
            a.setStatus(AppointmentStatus.valueOf(p[4]));
            a.setPaid(Boolean.parseBoolean(p[6]));
            HospitalManager.getAppointments().add(a);

            // link the appointment back to the patient and block the doctors slot
            try {
                Patient patient = HospitalManager.findPatient(a.getPatientId());
                patient.addAppointment(a);
            } catch (Exception e) {
                // patient not found, skip
            }
            Doctor d = HospitalManager.findDoctor(a.getDoctorId());
            if (d != null && a.getStatus() == AppointmentStatus.SCHEDULED) {
                d.getTimeSlots().add(a.getTimeSlot());
            }
        }
    }

    private static void loadPrescriptions() {
        ArrayList<String> lines = readLines("prescriptions.csv");
        for (int i = 0; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",");
            // recordId,date,patientId,medicine,dosage,duration
            Prescription pres = new Prescription(p[0], p[1], p[2], p[3], p[4], p[5]);
            HospitalManager.getRecords().add(pres);
            try {
                Patient patient = HospitalManager.findPatient(pres.getPatientId());
                patient.addRecord(pres);
            } catch (Exception e) {
                // patient not found, skip
            }
        }
    }

    private static void loadLabReports() {
        ArrayList<String> lines = readLines("labreports.csv");
        for (int i = 0; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",");
            // recordId,date,patientId,testName,result,referenceRange
            LabReport report = new LabReport(p[0], p[1], p[2], p[3], p[4], p[5]);
            HospitalManager.getRecords().add(report);
            try {
                Patient patient = HospitalManager.findPatient(report.getPatientId());
                patient.addRecord(report);
            } catch (Exception e) {
                // patient not found, skip
            }
        }
    }

    // ---------- saving ----------

    public static void saveAll() {
        saveUsers();
        saveDoctors();
        savePatients();
        saveStaff();
        saveAppointments();
        saveRecords();
    }

    public static void saveUsers() {
        writeLines("users.csv", Authentication.getUsers());
    }

    private static void saveDoctors() {
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<Doctor> doctors = HospitalManager.getDoctors();
        for (int i = 0; i < doctors.size(); i++) {
            Doctor d = doctors.get(i);
            lines.add(d.getId() + "," + d.getName() + "," + d.getAge() + "," + d.getPhone() + ","
                    + d.getSpecialization() + "," + d.getConsultationFee());
        }
        writeLines("doctors.csv", lines);
    }

    private static void savePatients() {
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<Patient> patients = HospitalManager.getPatients();
        for (int i = 0; i < patients.size(); i++) {
            Patient p = patients.get(i);
            lines.add(p.getId() + "," + p.getName() + "," + p.getAge() + "," + p.getPhone() + "," + p.getDisease());
        }
        writeLines("patients.csv", lines);
    }

    private static void saveStaff() {
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<Staff> staffList = HospitalManager.getStaffList();
        for (int i = 0; i < staffList.size(); i++) {
            Staff s = staffList.get(i);
            lines.add(s.getId() + "," + s.getName() + "," + s.getAge() + "," + s.getPhone() + ","
                    + s.getRole() + "," + s.getSalary());
        }
        writeLines("staff.csv", lines);
    }

    private static void saveAppointments() {
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<Appointment> appointments = HospitalManager.getAppointments();
        for (int i = 0; i < appointments.size(); i++) {
            Appointment a = appointments.get(i);
            lines.add(a.getAppointmentId() + "," + a.getPatientId() + "," + a.getDoctorId() + ","
                    + a.getTimeSlot() + "," + a.getStatus() + "," + a.getFee() + "," + a.isPaid());
        }
        writeLines("appointments.csv", lines);
    }

    private static void saveRecords() {
        ArrayList<String> presLines = new ArrayList<String>();
        ArrayList<String> labLines = new ArrayList<String>();
        ArrayList<MedicalRecord> records = HospitalManager.getRecords();
        for (int i = 0; i < records.size(); i++) {
            MedicalRecord r = records.get(i);
            if (r instanceof Prescription) {
                Prescription p = (Prescription) r;
                presLines.add(p.getRecordId() + "," + p.getDate() + "," + p.getPatientId() + ","
                        + p.getMedicineName() + "," + p.getDosage() + "," + p.getDuration());
            } else if (r instanceof LabReport) {
                LabReport l = (LabReport) r;
                labLines.add(l.getRecordId() + "," + l.getDate() + "," + l.getPatientId() + ","
                        + l.getTestName() + "," + l.getResult() + "," + l.getReferenceRange());
            }
        }
        writeLines("prescriptions.csv", presLines);
        writeLines("labreports.csv", labLines);
    }
}
