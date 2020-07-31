package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class CSRSService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RequestEntityFactory requestEntityFactory;

    @Value("${csrs.deleteUrl}")
    private String csrsDeleteUrl;

    public ResponseEntity deleteCivilServant(String uid) {
        try {
            RequestEntity requestEntity = requestEntityFactory.createDeleteRequest(String.format(csrsDeleteUrl, uid));
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, Void.class);
            return responseEntity;
        } catch (RequestEntityException | RestClientException e) {
            log.error("Could not delete user from csrs service: " + e);
            return null;
        }
    }
}
