package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.cshr.client.HttpClient;
import uk.gov.cshr.domain.learning.Learning;

@Service
@Slf4j
public class CslService {

    private final HttpClient httpClient;
    private final RequestEntityFactory requestEntityFactory;
    private final String getRequiredLearningUrl;

    public CslService(HttpClient httpClient, RequestEntityFactory requestEntityFactory,
                      @Value("${cslService.getRequiredLearningUrl}") String getRequiredLearningUrl) {
        this.httpClient = httpClient;
        this.requestEntityFactory = requestEntityFactory;
        this.getRequiredLearningUrl = getRequiredLearningUrl;
    }

    public Learning getRequiredLearningForUser(String uid) {
        String url = String.format("%s/%s", getRequiredLearningUrl, uid);
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
}
