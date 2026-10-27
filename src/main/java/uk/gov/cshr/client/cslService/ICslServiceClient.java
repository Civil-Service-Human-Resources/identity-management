package uk.gov.cshr.client.cslService;


import uk.gov.cshr.domain.learning.Learning;
import uk.gov.cshr.domain.learning.UserLearningResponse;
import uk.gov.cshr.service.cslService.models.GetOptionalLearningRecordParams;
import uk.gov.cshr.service.csrs.FormattedOrganisationalUnitNames;

public interface ICslServiceClient {
    Learning getRequiredLearningForUser(String uid);

    UserLearningResponse getOtherLearningForUser(String uid, GetOptionalLearningRecordParams params);

    Learning getDetailedLearningForUser(String uid, String courseId);

    FormattedOrganisationalUnitNames getFormattedOrganisationNames();

}
