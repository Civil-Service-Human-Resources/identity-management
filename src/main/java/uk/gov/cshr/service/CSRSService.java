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

    @Value("${csrs.getAgencyTokenByDomainAndCodeUrl}")
    private String getAgencyTokenByDomainAndCodeUrl;

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
            log.error("Could not get Org Code from Civil Servant from csrs service: " + e);
            return null;
        } catch(Exception e) {
            log.error("Could not create request to get Org Code from Civil Servant from csrs service: " + e);
            return null;
        }
    }

    public ResponseEntity removeOrg() {
        try {
            RequestEntity requestEntity = requestEntityFactory.createGetRequest(String.format(csrsOrgCodeUrl));
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, String.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            log.error("Could not remove Org Code from Civil Servant from csrs service: " + e);
            return null;
        } catch(Exception e) {
            log.error("Could not create request to remove Org Code from Civil Servant from csrs service: " + e);
            return null;
        }
    }


    public ResponseEntity getAgencyTokenForCivilServant(String domain, String code) {
        try {
            RequestEntity requestEntity = requestEntityFactory.createGetRequest(String.format(getAgencyTokenByDomainAndCodeUrl, domain, code));
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, AgencyTokenResponseDTO.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            log.error("Could not get AgencyToken from csrs service: " + e);
            return null;
        } catch(Exception e) {
            log.error("Could not create request to get AgencyToken from csrs service: " + e);
            return null;
        }
    }

    public ResponseEntity updateAgencyTokenForCivilServant(String code, String domain, String token, boolean isRemoveUser) {
        // Do NOT catch any exception, this ensures any exception is handled by the caller which has to be part of a transaction.
        UpdateSpacesForAgencyTokenRequestDTO requestDTO = new UpdateSpacesForAgencyTokenRequestDTO();
        requestDTO.setCode(code);
        requestDTO.setDomain(domain);
        requestDTO.setToken(token);
        requestDTO.setRemoveUser(isRemoveUser);

        String requestURL = String.format(csrsAgencyTokenUrl);

        RequestEntity requestEntity = requestEntityFactory.createPutRequest(requestURL, requestDTO);
        ResponseEntity responseEntity = restTemplate.exchange(requestEntity, AgencyTokenResponseDTO.class);
        return responseEntity;
    }

}
