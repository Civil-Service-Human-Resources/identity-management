package uk.gov.cshr.client.reportService;

import uk.gov.cshr.domain.DeleteUserResults;
import uk.gov.cshr.domain.UpdateUserDetailsParams;
import uk.gov.cshr.domain.UpdateUserResult;

public interface IReportServiceClient {

    public UpdateUserResult deactivateRegisteredLearners(UpdateUserDetailsParams uids);

    public DeleteUserResults removeUserDetails(UpdateUserDetailsParams parameters);
}
