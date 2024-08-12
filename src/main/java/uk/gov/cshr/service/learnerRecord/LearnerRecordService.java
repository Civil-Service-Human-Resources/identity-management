package uk.gov.cshr.service.learnerRecord;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.client.HttpClient;
import uk.gov.cshr.service.RequestEntityException;
import uk.gov.cshr.service.RequestEntityFactory;

@Service
@Slf4j
public class LearnerRecordService {

    private final HttpClient httpClient;

    private final RequestEntityFactory requestEntityFactory;

    private final String learnerRecordDeleteUrl;

    public LearnerRecordService(HttpClient httpClient,
                                RequestEntityFactory requestEntityFactory,
                                @Value("${learnerRecord.deleteUrl}") String learnerRecordDeleteUrl
    ) {
        this.httpClient = httpClient;
        this.requestEntityFactory = requestEntityFactory;
        this.learnerRecordDeleteUrl = learnerRecordDeleteUrl;
    }

    public ResponseEntity<Void> deleteCivilServant(String uid) {
        RequestEntity<Void> requestEntity = requestEntityFactory.createDeleteRequest(String.format(learnerRecordDeleteUrl, uid));
        return httpClient.sendRequest(requestEntity, Void.class);
    }
}
