package storage;

import model.*;
import exceptions.InvalidPatientIDException;
import exceptions.AppointmentConflictException;
import java.util.ArrayList;

// the main manager class that keeps all the lists and does searching/booking etc
public class HospitalManager {

    private static ArrayList<Patient> patients = new ArrayList<Patient>();
    private static ArrayList<Doctor> doctors = new ArrayList<Doctor>();
    private static ArrayList<Staff> staffList = new ArrayList<Staff>();
    private static ArrayList<Appointment> appointments = new ArrayList<Appointment>();
    private static ArrayList<MedicalRecord> records = new ArrayList<MedicalRecord>();

    public static ArrayList<Patient> getPatients() {
        return patients;
    }

    public static ArrayList<Doctor> getDoctors() {
        return doctors;
    }

    public static ArrayList<Staff> getStaffList() {
        return staffList;
    }

    public static ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    public static ArrayList<MedicalRecord> getRecords() {
        return records;
    }

    // ---------- patients ----------

    public static String nextPatientId() {
        return "P" + (patients.size() + 1);
    }

    public static void addPatient(Patient p) {
        patients.add(p);
        FileManager.saveAll();
    }

    // search for a patient by id, throws our custom exception if invalid or not found
    public static Patient findPatient(String id) throws InvalidPatientIDException {
        if (id == null || id.trim().equals("")) {
            throw new InvalidPatientIDException("Patient ID is empty!");
        }
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getId().equals(id)) {
                return patients.get(i);
            }
        }
        throw new InvalidPatientIDException("No patient found with ID: " + id);
    }

    // ---------- doctors ----------

    public static void addDoctor(Doctor d) {
        doctors.add(d);
    }

    public static Doctor findDoctor(String id) {
        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i).getId().equals(id)) {
                return doctors.get(i);
            }
        }
        return null;
    }

    // ---------- appointments ----------

    public static String nextAppointmentId() {
        return "A" + (appointments.size() + 1);
    }

    // books an appointment, checks both doctor and patient for time conflicts
    public static Appointment bookAppointment(String patientId, String doctorId, String timeSlot)
            throws InvalidPatientIDException, AppointmentConflictException {

        Patient patient = findPatient(patientId);
        Doctor doctor = findDoctor(doctorId);

        // check if the patient already has an appointment at this time
        ArrayList<Appointment> patientAppts = patient.getAppointments();
        for (int i = 0; i < patientAppts.size(); i++) {
            Appointment a = patientAppts.get(i);
            if (a.getTimeSlot().equals(timeSlot) && a.getStatus() == AppointmentStatus.SCHEDULED) {
                throw new AppointmentConflictException("Patient " + patient.getName() + " already has an appointment at " + timeSlot);
            }
        }

        // this will throw AppointmentConflictException if the doctor is busy
        doctor.book(timeSlot);

        Appointment appt = new Appointment(nextAppointmentId(), patientId, doctorId, timeSlot, doctor.getConsultationFee());
        appointments.add(appt);
        patient.addAppointment(appt);
        FileManager.saveAll();
        return appt;
    }

    // moves an appointment to a new time slot, checks both doctor and patient for conflicts
    public static void rescheduleAppointment(Appointment appt, String newSlot)
            throws InvalidPatientIDException, AppointmentConflictException {

        Patient patient = findPatient(appt.getPatientId());

        // check if the patient already has another appointment at the new time
        ArrayList<Appointment> patientAppts = patient.getAppointments();
        for (int i = 0; i < patientAppts.size(); i++) {
            Appointment a = patientAppts.get(i);
            if (a != appt && a.getTimeSlot().equals(newSlot) && a.getStatus() == AppointmentStatus.SCHEDULED) {
                throw new AppointmentConflictException("Patient " + patient.getName() + " already has an appointment at " + newSlot);
            }
        }

        // this will throw AppointmentConflictException if the doctor is busy at the new time
        Doctor doctor = findDoctor(appt.getDoctorId());
        if (doctor != null) {
            doctor.reschedule(appt.getTimeSlot(), newSlot);
        }

        appt.reschedule(appt.getTimeSlot(), newSlot);
        FileManager.saveAll();
    }

    public static void cancelAppointment(Appointment appt) {
        Doctor doctor = findDoctor(appt.getDoctorId());
        if (doctor != null) {
            doctor.cancel(appt.getTimeSlot());
        }
        appt.cancel(appt.getTimeSlot());
        FileManager.saveAll();
    }

    // ---------- medical records ----------

    public static String nextRecordId() {
        return "R" + (records.size() + 1);
    }

    public static void addRecord(MedicalRecord record) throws InvalidPatientIDException {
        Patient patient = findPatient(record.getPatientId());
        patient.addRecord(record);
        records.add(record);
        FileManager.saveAll();
    }

    // add some default doctors the first time the program runs
    public static void seedDoctors() {
        if (doctors.size() == 0) {
            addDoctor(new Doctor("D1", "Dr. Rahman", 45, "01711111111", "Cardiology", 1500));
            addDoctor(new Doctor("D2", "Dr. Sultana", 38, "01722222222", "Medicine", 1000));
            addDoctor(new Doctor("D3", "Dr. Karim", 50, "01733333333", "Orthopedics", 1200));
            addDoctor(new Doctor("D4", "Dr. Akter", 35, "01744444444", "Pediatrics", 900));
            FileManager.saveAll();
        }
    }
}
