package exceptions;

// custom exception for when an appointment time slot is already taken
public class AppointmentConflictException extends Exception {

    public AppointmentConflictException(String message) {
        super(message);
    }
}
