package uk.gov.cshr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class CSRSService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSRSService.class);

    private final RestTemplate restTemplate;

    private final RequestEntityFactory requestEntityFactory;

    private final String csrsDeleteUrl;

    private final String csrsGetOrganisationsUrl;

    private final String csrsAddOrganisationReportingPermissionUrl;

    private final String civilServantWithReportingPermissionUrl;

    public CSRSService(@Value("${csrs.deleteUrl}") String csrsDeleteUrl,
                       @Value("${csrs.organisationListUrl}") String csrsGetOrganisationsUrl,
                       @Value("${csrs.addOrganisationReportingPermissionUrl}") String addOrganisationReportingPermissionUrl,
                       @Value("${csrs.civilServantWithReportingPermissionUrl}")  String civilServantWithReportingPermissionUrl,
                       RestTemplate restTemplate,
                       RequestEntityFactory requestEntityFactory
    ) {
        this.restTemplate = restTemplate;
        this.requestEntityFactory = requestEntityFactory;
        this.csrsDeleteUrl = csrsDeleteUrl;
        this.csrsGetOrganisationsUrl = csrsGetOrganisationsUrl;
        this.csrsAddOrganisationReportingPermissionUrl = addOrganisationReportingPermissionUrl;
        this.civilServantWithReportingPermissionUrl = civilServantWithReportingPermissionUrl;
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

    public ResponseEntity getCivilServantUIDsWithReportingPermission() {
        try {
            RequestEntity requestEntity = requestEntityFactory.createGetRequest(civilServantWithReportingPermissionUrl);
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, Object.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            LOGGER.error("Could not get users with reporting permission " + e);
            return null;
        }
    }

    public ResponseEntity getOrganisations() {
        try {
            RequestEntity requestEntity = requestEntityFactory.createGetRequest(csrsGetOrganisationsUrl);
            ResponseEntity responseEntity = restTemplate.exchange(requestEntity, Object.class);
            return responseEntity;
        } catch(RequestEntityException | RestClientException e) {
            LOGGER.error("Could not get organisations from csrs service: " + e);
            return null;
        }
    }

    public ResponseEntity addOrganisationReportingPermission(String uid, List<String> organisationIds) {
        try {
            RequestEntity requestEntity = requestEntityFactory.createPostRequest(String.format(csrsAddOrganisationReportingPermissionUrl, uid), organisationIds);
            return restTemplate.exchange(requestEntity, Object.class);
        } catch(RequestEntityException | RestClientException e) {
            LOGGER.error("Could not add organisations from csrs service: " + e);
            return null;
        }
    }
}
