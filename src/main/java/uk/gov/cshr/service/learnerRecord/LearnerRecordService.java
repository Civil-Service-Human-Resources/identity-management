package uk.gov.cshr.service.learnerRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.service.RequestEntityException;
import uk.gov.cshr.service.RequestEntityFactory;

@Service
public class LearnerRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LearnerRecordService.class);

    private final RestTemplate restTemplate;

    private final RequestEntityFactory requestEntityFactory;

    private final String learnerRecordDeleteUrl;

    public LearnerRecordService(RestTemplate restTemplate,
                                RequestEntityFactory requestEntityFactory,
                                @Value("learnerRecord.deleteUrl") String learnerRecordDeleteUrl
    ) {
        this.restTemplate = restTemplate;
        this.requestEntityFactory = requestEntityFactory;
        this.learnerRecordDeleteUrl = learnerRecordDeleteUrl;
    }

    public ResponseEntity deleteCivilServant(String uid) {
        try {
            RequestEntity requestEntity = requestEntityFactory.createDeleteRequest(String.format(learnerRecordDeleteUrl, uid));
            ResponseEntity<Void> reponseEntity = restTemplate.exchange(requestEntity, Void.class);
            return reponseEntity;
        } catch (RequestEntityException | RestClientException e) {
            LOGGER.error("Could not delete user from learner record: " + e);
            return null;
        }
    }
}
