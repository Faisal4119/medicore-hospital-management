package interfaces;

import exceptions.AppointmentConflictException;

// interface for booking, cancelling and rescheduling time slots
public interface Schedulable {
    void book(String timeSlot) throws AppointmentConflictException;

    void cancel(String timeSlot);

    void reschedule(String oldSlot, String newSlot) throws AppointmentConflictException;
}
