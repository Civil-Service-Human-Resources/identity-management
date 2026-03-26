package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.cshr.client.HttpClient;
import uk.gov.cshr.domain.learning.Learning;
import uk.gov.cshr.domain.learning.UserLearningResponse;
import uk.gov.cshr.service.csrs.FormattedOrganisationalUnitNames;

@Service
@Slf4j
public class CslService {

    private final HttpClient httpClient;
    private final RequestEntityFactory requestEntityFactory;
    private final String getRequiredLearningUrl;
    private final String getFormattedOrganisationNamesUrl;
    private final String getUserLearningUrl;
    private final String getDetailedLearningUrl;

    public CslService(HttpClient httpClient, RequestEntityFactory requestEntityFactory,
                      @Value("${cslService.getRequiredLearningUrl}") String getRequiredLearningUrl,
                      @Value("${cslService.getFormattedOrganisationNames}") String getFormattedOrganisationNamesUrl,
                      @Value("${cslService.getUserLearningUrl}") String getUserLearningUrl,
                      @Value("${cslService.getDetailedLearningUrl}") String getDetailedLearningUrl) {
        this.httpClient = httpClient;
        this.requestEntityFactory = requestEntityFactory;
        this.getRequiredLearningUrl = getRequiredLearningUrl;
        this.getFormattedOrganisationNamesUrl = getFormattedOrganisationNamesUrl;
        this.getUserLearningUrl = getUserLearningUrl;
        this.getDetailedLearningUrl = getDetailedLearningUrl;
    }

    public Learning getRequiredLearningForUser(String uid) {
        String url = String.format("%s/%s", getRequiredLearningUrl, uid);
        return getLearning(url);
    }

    private Learning getLearning(String url) {
        RequestEntity<Void> requestEntity = requestEntityFactory.createGetRequest(url);
        try {
            return httpClient.sendRequestNoRetries(requestEntity, Learning.class).getBody();
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    public UserLearningResponse getOtherLearningForUser(String uid, int page, int size) {
        String url = String.format("%s/%s?page=%d&size=%d", getUserLearningUrl, uid, page, size);
        RequestEntity<Void> requestEntity = requestEntityFactory.createGetRequest(url);
        try {
            return httpClient.sendRequestNoRetries(requestEntity, UserLearningResponse.class).getBody();
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    public Learning getDetailedLearningForUser(String uid, String courseId) {
        String url = String.format("%s/%s?courseIds=%s", getDetailedLearningUrl, uid, courseId);
        return getLearning(url);
    }

    public FormattedOrganisationalUnitNames getFormattedOrganisationNames() {
        RequestEntity<Void> requestEntity = requestEntityFactory.createGetRequest(getFormattedOrganisationNamesUrl);
        try {
            return httpClient.sendRequestNoRetries(requestEntity,
                    FormattedOrganisationalUnitNames.class).getBody();
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 404) {
                return null;
            }
            throw e;
        }
    }
}
