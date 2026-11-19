package exceptions;

// custom exception for when a patient id is wrong or not found
public class InvalidPatientIDException extends Exception {

    public InvalidPatientIDException(String message) {
        super(message);
    }
}
