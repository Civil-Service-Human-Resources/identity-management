package uk.gov.cshr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.cshr.dto.AgencyTokenDTO;

@Service
public class CSRSService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSRSService.class);

    private final RestTemplate restTemplate;

    private final RequestEntityFactory requestEntityFactory;

    private final String csrsDeleteUrl;

    private final String csrsGetAgencyTokenUrl;

    public CSRSService(@Value("${csrs.deleteUrl}") String csrsDeleteUrl,
                       @Value("${csrs.getAgencyTokenUrl}") String csrsGetAgencyTokenUrl,
                       RestTemplate restTemplate,
                       RequestEntityFactory requestEntityFactory
    ) {
        this.restTemplate = restTemplate;
        this.requestEntityFactory = requestEntityFactory;
        this.csrsDeleteUrl = csrsDeleteUrl;
        this.csrsGetAgencyTokenUrl = csrsGetAgencyTokenUrl;
    }

    public ResponseEntity deleteCivilServant(String uid) {
        try {
            RequestEntity requestEntity = requestEntityFactory.createDeleteRequest(String.format(csrsDeleteUrl, uid));
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, Void.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            LOGGER.error("Could not delete user from csrs service: " + e);
            return null;
        }
    }

    public ResponseEntity getAgencyTokenForCivilServant(String domain, String email) {

        String requestURL = String.format(csrsGetAgencyTokenUrl, domain, email);
        System.out.println(requestURL);

        try {
            RequestEntity requestEntity = requestEntityFactory.createGetRequest(requestURL);
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, AgencyTokenDTO.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            LOGGER.error("Could not get AgencyToken from csrs service: " + e);
            return null;
        }

    }
}
