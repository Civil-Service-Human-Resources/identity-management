package uk.gov.cshr.client.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ErrorDto {
    private Instant timestamp = Instant.now();
    private List<String> errors = new ArrayList<>();
    private int status;
    private String message;

    @JsonIgnore
    public ResponseEntity<Object> getAsResponseEntity() {
        return new ResponseEntity<>(this, HttpStatusCode.valueOf(getStatus()));
    }
}
