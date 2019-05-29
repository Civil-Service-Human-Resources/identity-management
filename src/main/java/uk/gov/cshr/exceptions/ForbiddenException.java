package uk.gov.cshr.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, code = HttpStatus.FORBIDDEN, reason = "foo")
public class ForbiddenException extends RuntimeException {

}