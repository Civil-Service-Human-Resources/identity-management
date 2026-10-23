package uk.gov.cshr.client.exception;

import lombok.Getter;
import uk.gov.cshr.client.model.ErrorDto;

@Getter
public class ValidationException extends RuntimeException {
    private final ErrorDto errorDto;

    public ValidationException(String message) {
        super(message);
        this.errorDto = null;
    }

    public ValidationException(String message, ErrorDto errorDto) {
        super(message);
        this.errorDto = errorDto;
    }
}
