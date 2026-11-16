package uk.gov.cshr.service.reportingService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.reportService.IReportServiceClient;
import uk.gov.cshr.domain.DeleteUserResults;
import uk.gov.cshr.domain.UpdateUserDetailsParams;
import uk.gov.cshr.domain.UpdateUserResult;

import java.util.Collections;
import java.util.List;

import static uk.gov.cshr.utils.Util.batchList;

@Service
@Slf4j
public class ReportingService {
    private final IReportServiceClient client;
    private final Integer deactivateRegisteredLearnersBatchSize;

    public ReportingService(
            IReportServiceClient client,
            @Value("${reporting.registeredLearners.deactivationBatchSize}") Integer deactivateRegisteredLearnersBatchSize
    ) {
        this.client = client;
        this.deactivateRegisteredLearnersBatchSize = deactivateRegisteredLearnersBatchSize;
    }

    public UpdateUserResult deactivateRegisteredLearners(List<String> uids) {
        UpdateUserResult totalResults = new UpdateUserResult(0);
        log.info("Deactivating {} total users in report service", uids.size());
        batchList(uids, deactivateRegisteredLearnersBatchSize).forEach(batchedUids -> {
            log.info("Deactivating registered learners in report service: {}", batchedUids);
            UpdateUserDetailsParams parameters = new UpdateUserDetailsParams(batchedUids);
            UpdateUserResult response = client.deactivateRegisteredLearners(parameters);
            totalResults.add(response);
        });
        log.info("Updated rows from deactivating registered learners: {}", totalResults.getAffectedRows());
        return totalResults;
    }

    public DeleteUserResults removeUserDetails(String uid) {
        log.info("Removing user details from report service: {}", uid);
        UpdateUserDetailsParams parameters = new UpdateUserDetailsParams(Collections.singletonList(uid));
        DeleteUserResults response = client.removeUserDetails(parameters);
        log.info("Response from remove user details request: {}", response);
        return response;
    }

}
