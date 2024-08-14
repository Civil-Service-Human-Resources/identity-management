package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.HttpClient;

@Service
@Slf4j
public class CslService {

    private final HttpClient httpClient;
    private final RequestEntityFactory requestEntityFactory;

    public CslService(HttpClient httpClient, RequestEntityFactory requestEntityFactory) {
        this.httpClient = httpClient;
        this.requestEntityFactory = requestEntityFactory;
    }

    public void getRequiredLearningForUser(String uid) {

    }
}
