package uk.gov.cshr.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.client.exception.NotFoundException;
import uk.gov.cshr.client.exception.ValidationException;
import uk.gov.cshr.client.model.ErrorDto;

@Slf4j
@AllArgsConstructor
@Getter
public class HttpClient implements IHttpClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final RestTemplate restTemplate;

    private <T, R> ResponseEntity<T> executeRawRequest(RequestEntity<R> request, Class<T> responseClass) {
        try {
            log.debug("Sending request: {}", request);
            ResponseEntity<T> response = restTemplate.exchange(request, responseClass);
            log.debug("Request response: {}", response);
            return response;
        } catch (RestClientResponseException e) {
            throw handleRestClientException(e);
        }
    }

    private RuntimeException handleRestClientException(RestClientResponseException e) {
        String msg = String.format("Error sending request. Status code: %s", e.getStatusCode());
        if (!e.getResponseBodyAsString().isEmpty()) {
            msg = String.format("%s Body was: %s.", msg, e.getResponseBodyAsString());
        }
        msg = String.format("%s Error was: %s", msg, e.getMessage());
        log.error(msg);
        if (e.getStatusCode().equals(HttpStatusCode.valueOf(404))) {
            throw new NotFoundException(msg);
        } else if (e.getStatusCode().equals(HttpStatusCode.valueOf(400))) {
            ErrorDto errorDto = parseErrorDto(e.getResponseBodyAsString());
            throw new ValidationException(msg, errorDto);
        }
        throw new RuntimeException(msg);
    }

    private ErrorDto parseErrorDto(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            ErrorDto errorDto = OBJECT_MAPPER.readValue(responseBody, ErrorDto.class);
            if (errorDto.getMessage() != null && !errorDto.getMessage().isBlank()
                    && errorDto.getErrors() != null && !errorDto.getErrors().isEmpty()
                    && errorDto.getStatus() != 0) {
                return errorDto;
            }
        } catch (Exception ex) {
            log.debug("Could not parse error response body as ErrorDto: {}", ex.getMessage());
        }
        return null;
    }

    @Override
    public <T, R> T executeRequest(RequestEntity<R> request, Class<T> responseClass) {
        return executeRawRequest(request, responseClass).getBody();
    }

    @Override
    public <T, R> T executeTypeReferenceRequest(RequestEntity<R> request, ParameterizedTypeReference<T> parameterizedTypeReference) {
        try {
            log.debug("Sending request: {}", request);
            ResponseEntity<T> response = restTemplate.exchange(request, parameterizedTypeReference);
            log.debug("Request response: {}", response);
            return response.getBody();
        } catch (RestClientResponseException e) {
            throw handleRestClientException(e);
        }

    }

}
