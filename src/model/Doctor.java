package model;

import interfaces.Schedulable;
import exceptions.AppointmentConflictException;
import java.util.ArrayList;

// represents a doctor with specialization, fee and their booked time slots
public class Doctor extends Person implements Schedulable {
    private String specialization;
    private double consultationFee;
    private ArrayList<String> timeSlots; // slots that are already booked

    public Doctor(String id, String name, int age, String phone, String specialization, double consultationFee) {
        super(id, name, age, phone);
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.timeSlots = new ArrayList<String>();
    }

    // book a slot for this doctor, throws exception if already taken
    @Override
    public void book(String timeSlot) throws AppointmentConflictException {
        for (int i = 0; i < timeSlots.size(); i++) {
            if (timeSlots.get(i).equals(timeSlot)) {
                throw new AppointmentConflictException("Dr. " + name + " already has an appointment at " + timeSlot);
            }
        }
        timeSlots.add(timeSlot);
    }

    @Override
    public void cancel(String timeSlot) {
        timeSlots.remove(timeSlot);
    }

    @Override
    public void reschedule(String oldSlot, String newSlot) throws AppointmentConflictException {
        // first check the new slot is free, then move
        for (int i = 0; i < timeSlots.size(); i++) {
            if (timeSlots.get(i).equals(newSlot)) {
                throw new AppointmentConflictException("Dr. " + name + " already has an appointment at " + newSlot);
            }
        }
        timeSlots.remove(oldSlot);
        timeSlots.add(newSlot);
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public ArrayList<String> getTimeSlots() {
        return timeSlots;
    }
}
