package uk.gov.cshr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.dto.AgencyTokenResponseDTO;
import uk.gov.cshr.dto.UpdateSpacesForAgencyTokenDTO;

@Service
public class CSRSService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSRSService.class);

    private final RestTemplate restTemplate;

    private final RequestEntityFactory requestEntityFactory;

    private final String csrsDeleteUrl;

    private final String csrsAgencyTokenUrl;

    public CSRSService(@Value("${csrs.deleteUrl}") String csrsDeleteUrl,
                       @Value("${csrs.agencyTokenUrl}") String csrsAgencyTokenUrl,
                       RestTemplate restTemplate,
                       RequestEntityFactory requestEntityFactory
    ) {
        this.restTemplate = restTemplate;
        this.requestEntityFactory = requestEntityFactory;
        this.csrsDeleteUrl = csrsDeleteUrl;
        this.csrsAgencyTokenUrl = csrsAgencyTokenUrl;
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

    public ResponseEntity getAgencyTokenForCivilServant(String domain, String code) {

        String requestURL = String.format(csrsAgencyTokenUrl, domain, code);
        System.out.println(requestURL);

        try {
            RequestEntity requestEntity = requestEntityFactory.createGetRequest(requestURL);
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, AgencyTokenResponseDTO.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            LOGGER.error("Could not get AgencyToken from csrs service: " + e);
            return null;
        }

    }

    public ResponseEntity updateAgencyTokenForCivilServant(String code, String domain, String token) {

        String requestURL = String.format(csrsAgencyTokenUrl);
        System.out.println(requestURL);
        UpdateSpacesForAgencyTokenDTO requestDTO = new UpdateSpacesForAgencyTokenDTO();
        requestDTO.setCode(code);
        requestDTO.setDomain(domain);
        requestDTO.setToken(token);
        requestDTO.setRemoveUser(true);

        try {
            RequestEntity requestEntity = requestEntityFactory.createPutRequest(requestURL, requestDTO);
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, AgencyTokenResponseDTO.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            LOGGER.error("Could not update quota on AgencyToken from csrs service: " + e);
            return null;
        }

    }
}
