package uk.gov.cshr.client.learnerRecord;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import uk.gov.cshr.client.IHttpClient;


@Slf4j
@Component
public class LearnerRecordClient implements ILearnerRecordClient {

    private final IHttpClient httpClient;
    private final String learnerRecordDeleteUrl;

    public LearnerRecordClient(@Qualifier("learnerRecordHttpClient") IHttpClient httpClient,
                               @Value("${learnerRecord.deleteUrl}") String learnerRecordDeleteUrl) {
        this.httpClient = httpClient;
        this.learnerRecordDeleteUrl = learnerRecordDeleteUrl;
    }


    @Override
    public void deleteCivilServant(String uid) {
        RequestEntity<Void> request = RequestEntity.delete(String.format(learnerRecordDeleteUrl, uid)).build();
        httpClient.executeRequest(request, Void.class);
    }
}
