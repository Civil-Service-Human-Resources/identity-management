package uk.gov.cshr.service.learnerRecord;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.learnerRecord.ILearnerRecordClient;

@Service
@Slf4j
public class LearnerRecordService {

    private final ILearnerRecordClient client;

    public LearnerRecordService(ILearnerRecordClient client) {

        this.client = client;
    }

    public void deleteCivilServant(String uid) {
        client.deleteCivilServant(uid);
    }
}
