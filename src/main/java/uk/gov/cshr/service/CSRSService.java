package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.dto.AgencyTokenResponseDTO;
import uk.gov.cshr.dto.UpdateSpacesForAgencyTokenRequestDTO;

@Slf4j
@Service
public class CSRSService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RequestEntityFactory requestEntityFactory;

    @Value("${csrs.deleteUrl}")
    private String csrsDeleteUrl;

    @Value("${csrs.agencyTokenUrl}")
    private String csrsAgencyTokenUrl;

    @Value("${csrs.orgCodeUrl}")
    private String csrsOrgCodeUrl;

    public ResponseEntity deleteCivilServant(String uid) {
        try {
            RequestEntity requestEntity = requestEntityFactory.createDeleteRequest(String.format(csrsDeleteUrl, uid));
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, Void.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            log.error("Could not delete user from csrs service: " + e);
            return null;
        }
    }

    public ResponseEntity getOrganisationCodeForCivilServant(String uid) {
        try {
            RequestEntity requestEntity = requestEntityFactory.createGetRequest(String.format(csrsOrgCodeUrl, uid));
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, String.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            log.error("Could not get Org Code fro Civil Servant from csrs service: " + e);
            return null;
        }

    }

    public ResponseEntity getAgencyTokenForCivilServant(String domain, String code) {
        try {
            RequestEntity requestEntity = requestEntityFactory.createGetRequest(String.format(csrsAgencyTokenUrl, domain, code));
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, AgencyTokenResponseDTO.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            log.error("Could not get AgencyToken from csrs service: " + e);
            return null;
        }

    }

    public ResponseEntity updateAgencyTokenForCivilServant(String code, String domain, String token) {
        UpdateSpacesForAgencyTokenRequestDTO requestDTO = new UpdateSpacesForAgencyTokenRequestDTO();
        requestDTO.setCode(code);
        requestDTO.setDomain(domain);
        requestDTO.setToken(token);
        requestDTO.setRemoveUser(true);

        try {
            RequestEntity requestEntity = requestEntityFactory.createPutRequest(String.format(csrsAgencyTokenUrl), requestDTO);
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, AgencyTokenResponseDTO.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            log.error("Could not update quota on AgencyToken from csrs service: " + e);
            return null;
        }

    }

}
