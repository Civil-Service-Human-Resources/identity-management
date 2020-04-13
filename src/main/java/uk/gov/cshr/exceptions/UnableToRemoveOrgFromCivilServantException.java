package uk.gov.cshr.exceptions;

public class UnableToRemoveOrgFromCivilServantException extends RuntimeException {

    public UnableToRemoveOrgFromCivilServantException(String message) {
        super(message);
    }

    public UnableToRemoveOrgFromCivilServantException(String message, Throwable cause) {
        super(message, cause);
    }
}
