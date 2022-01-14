package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.client.HttpClient;

@Slf4j
@Service
public class CSRSService {

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private RequestEntityFactory requestEntityFactory;

    @Value("${csrs.deleteUrl}")
    private String csrsDeleteUrl;

    public ResponseEntity<Void> deleteCivilServant(String uid) {
        RequestEntity<Void> requestEntity = requestEntityFactory.createDeleteRequest(String.format(csrsDeleteUrl, uid));
        return httpClient.sendRequest(requestEntity);
    }
}
