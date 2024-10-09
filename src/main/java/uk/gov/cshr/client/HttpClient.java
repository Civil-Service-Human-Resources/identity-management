package uk.gov.cshr.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.service.RequestEntityException;

@Service
@Slf4j
public class HttpClient {

    private final int MAX_RETRIES = 3;

    private final RestTemplate restTemplate;

    public HttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T, R> ResponseEntity<T> sendRequestNoRetries(RequestEntity<R> requestEntity, Class<T> responseClass) {
        try {
            ResponseEntity<T> resp = restTemplate.exchange(requestEntity, responseClass);
            log.debug(String.format("Response from %s %s: %s", requestEntity.getMethod(), requestEntity.getUrl(), resp.toString()));
            return resp;
        } catch (RequestEntityException | RestClientResponseException e) {
            log.error(String.format("Failed to send request with %d retries: method: %s, URL: %s", MAX_RETRIES, requestEntity.getMethod(), requestEntity.getUrl()));
            throw e;
        }
    }

    public <T, R> ResponseEntity<T> sendRequest(RequestEntity<R> requestEntity, Class<T> responseClass) {
        int count = 0;
        while (true) {
            try {
                return restTemplate.exchange(requestEntity, responseClass);
            } catch (RequestEntityException | RestClientResponseException e) {
                if (++count == MAX_RETRIES) {
                    log.error(String.format("Failed to send request with %d retries: method: %s, URL: %s", MAX_RETRIES, requestEntity.getMethod(), requestEntity.getUrl()));
                    throw e;
                }
            }
        }
    }

}
