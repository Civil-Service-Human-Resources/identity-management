package uk.gov.cshr.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.service.RequestEntityException;

import javax.ws.rs.core.Response;

@Service
@Slf4j
public class HttpClient {

    private final int MAX_RETRIES = 3;

    private final RestTemplate restTemplate;

    public HttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Void> sendRequest(RequestEntity<Void> requestEntity) {
        int count = 0;
        while (true) {
            try {
                return restTemplate.exchange(requestEntity, Void.class);
            } catch (RequestEntityException | RestClientException e) {
                if (++count == MAX_RETRIES) {
                    log.error(String.format("Failed to send request with %d retries: method: %s, URL: %s", MAX_RETRIES, requestEntity.getMethod(), requestEntity.getUrl()));
                    log.error(String.format("Exception: %s", e));
                    return null;
                }
            }
        }
    }

}
