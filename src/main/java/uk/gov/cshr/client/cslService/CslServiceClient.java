package uk.gov.cshr.client.cslService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.cshr.client.IHttpClient;
import uk.gov.cshr.domain.learning.Learning;
import uk.gov.cshr.domain.learning.UserLearningResponse;
import uk.gov.cshr.service.cslService.models.GetOptionalLearningRecordParams;
import uk.gov.cshr.service.csrs.FormattedOrganisationalUnitNames;

@Component
@Slf4j
public class CslServiceClient implements ICslServiceClient {

    private final IHttpClient httpClient;
    private final String getRequiredLearningUrl;
    private final String getFormattedOrganisationNamesUrl;
    private final String getUserLearningUrl;
    private final String getDetailedLearningUrl;

    public CslServiceClient(@Qualifier("cslServiceHttpClient") IHttpClient httpClient, @Value("${cslService.getRequiredLearningUrl}") String getRequiredLearningUrl,
                            @Value("${cslService.getFormattedOrganisationNames}") String getFormattedOrganisationNamesUrl,
                            @Value("${cslService.getUserLearningUrl}") String getUserLearningUrl,
                            @Value("${cslService.getDetailedLearningUrl}") String getDetailedLearningUrl) {
        this.httpClient = httpClient;
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
        RequestEntity<Void> requestEntity = RequestEntity.get(url).build();
        try {
            return httpClient.executeRequest(requestEntity, Learning.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().equals(HttpStatusCode.valueOf(404))) {
                return null;
            }
            throw e;
        }
    }

    public UserLearningResponse getOtherLearningForUser(String uid, GetOptionalLearningRecordParams params) {
        String url = UriComponentsBuilder.fromPath(String.format("%s/%s", getUserLearningUrl, uid))
                .queryParam("page", params.getPage())
                .queryParam("size", params.getSize())
                .queryParam("q", params.getQ())
                .queryParam("sort", params.getSortDirection()).toUriString();
        RequestEntity<Void> requestEntity = RequestEntity.get(url).build();
        try {
            return httpClient.executeRequest(requestEntity, UserLearningResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().equals(HttpStatusCode.valueOf(404))) {
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
        RequestEntity<Void> requestEntity = RequestEntity.get(getFormattedOrganisationNamesUrl).build();
        try {
            return httpClient.executeRequest(requestEntity, FormattedOrganisationalUnitNames.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().equals(HttpStatusCode.valueOf(404))) {
                return null;
            }
            throw e;
        }
    }

}
