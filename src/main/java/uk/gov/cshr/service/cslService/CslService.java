package uk.gov.cshr.service.cslService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.cslService.ICslServiceClient;
import uk.gov.cshr.domain.learning.Learning;
import uk.gov.cshr.domain.learning.UserLearningResponse;
import uk.gov.cshr.service.cslService.models.GetOptionalLearningRecordParams;
import uk.gov.cshr.service.csrs.FormattedOrganisationalUnitNames;

@Service
@Slf4j
public class CslService {

    private final ICslServiceClient client;

    public CslService(ICslServiceClient client) {
        this.client = client;
    }

    public Learning getRequiredLearningForUser(String uid) {
        return client.getRequiredLearningForUser(uid);
    }

    public UserLearningResponse getOtherLearningForUser(String uid, GetOptionalLearningRecordParams params) {
        return client.getOtherLearningForUser(uid, params);
    }

    public Learning getDetailedLearningForUser(String uid, String courseId) {
        return client.getDetailedLearningForUser(uid, courseId);
    }

    public FormattedOrganisationalUnitNames getFormattedOrganisationNames() {
        return client.getFormattedOrganisationNames();
    }
}
