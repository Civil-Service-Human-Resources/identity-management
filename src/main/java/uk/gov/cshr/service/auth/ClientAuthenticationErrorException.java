package uk.gov.cshr.service.auth;

public class ClientAuthenticationErrorException extends RuntimeException {
    public ClientAuthenticationErrorException(String message) {
        super(message);
    }
}
