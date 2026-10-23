package uk.gov.cshr.client.exception;

public class InternalAuthErrorException extends RuntimeException {
    public InternalAuthErrorException(String message) {
        super(message);
    }
}
