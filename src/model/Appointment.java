package model;

import interfaces.Billable;
import interfaces.Schedulable;
import exceptions.AppointmentConflictException;

// links a patient and a doctor to a time slot, also billable per visit
public class Appointment implements Schedulable, Billable {
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String timeSlot;
    private AppointmentStatus status;
    private double fee;
    private boolean paid;

    public Appointment(String appointmentId, String patientId, String doctorId, String timeSlot, double fee) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.timeSlot = timeSlot;
        this.fee = fee;
        this.status = AppointmentStatus.SCHEDULED;
        this.paid = false;
    }

    @Override
    public double calculateBill() {
        // cancelled appointments dont cost anything
        if (status == AppointmentStatus.CANCELLED) {
            return 0;
        }
        return fee;
    }

    @Override
    public void book(String timeSlot) throws AppointmentConflictException {
        this.timeSlot = timeSlot;
        this.status = AppointmentStatus.SCHEDULED;
    }

    @Override
    public void cancel(String timeSlot) {
        this.status = AppointmentStatus.CANCELLED;
    }

    @Override
    public void reschedule(String oldSlot, String newSlot) throws AppointmentConflictException {
        this.timeSlot = newSlot;
        this.status = AppointmentStatus.SCHEDULED;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public double getFee() {
        return fee;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
