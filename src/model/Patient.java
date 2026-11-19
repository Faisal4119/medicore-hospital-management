package model;

import interfaces.Billable;
import java.util.ArrayList;

// represents a patient in the hospital, keeps their medical records
public class Patient extends Person implements Billable {
    private String disease;
    private ArrayList<MedicalRecord> records;
    private ArrayList<Appointment> appointments;

    public Patient(String id, String name, int age, String phone, String disease) {
        super(id, name, age, phone);
        this.disease = disease;
        this.records = new ArrayList<MedicalRecord>();
        this.appointments = new ArrayList<Appointment>();
    }

    // total bill = sum of all unpaid appointments of this patient
    @Override
    public double calculateBill() {
        double total = 0;
        for (int i = 0; i < appointments.size(); i++) {
            Appointment a = appointments.get(i);
            if (!a.isPaid() && a.getStatus() != AppointmentStatus.CANCELLED) {
                total = total + a.calculateBill();
            }
        }
        return total;
    }

    public void addRecord(MedicalRecord record) {
        records.add(record);
    }

    public void addAppointment(Appointment a) {
        appointments.add(a);
    }

    public ArrayList<MedicalRecord> getRecords() {
        return records;
    }

    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }
}
