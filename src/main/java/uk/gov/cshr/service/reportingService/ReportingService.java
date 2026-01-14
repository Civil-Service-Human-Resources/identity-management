package uk.gov.cshr.service.reportingService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.HttpClient;
import uk.gov.cshr.domain.DeleteUserResults;
import uk.gov.cshr.domain.UpdateUserDetailsParams;
import uk.gov.cshr.domain.UpdateUserResult;
import uk.gov.cshr.service.RequestEntityFactory;

import java.util.Collections;
import java.util.List;

import static uk.gov.cshr.utils.Util.batchList;

@Service
@Slf4j
public class ReportingService {
    private final HttpClient httpClient;
    private final RequestEntityFactory requestEntityFactory;
    private final String deactivateRegisteredLearnersUrl;
    private final Integer deactivateRegisteredLearnersBatchSize;
    private final String removeUserDataFromReportUrl;

    public ReportingService(
            HttpClient httpClient,
            RequestEntityFactory requestEntityFactory,
            @Value("${reporting.registeredLearners.deactivateUrl}") String deactivateRegisteredLearnersUrl,
            @Value("${reporting.registeredLearners.deactivationBatchSize}") Integer deactivateRegisteredLearnersBatchSize,
            @Value("${reporting.api.removeUserDataFromReportUrl}") String removeUserDataFromReportUrl
    ) {
        this.httpClient = httpClient;
        this.requestEntityFactory = requestEntityFactory;
        this.deactivateRegisteredLearnersUrl = deactivateRegisteredLearnersUrl;
        this.deactivateRegisteredLearnersBatchSize = deactivateRegisteredLearnersBatchSize;
        this.removeUserDataFromReportUrl = removeUserDataFromReportUrl;
    }

    public UpdateUserResult deactivateRegisteredLearners(List<String> uids) {
        UpdateUserResult totalResults = new UpdateUserResult(0);
        log.info("Deactivating {} total users in report service", uids.size());
        batchList(uids, deactivateRegisteredLearnersBatchSize).forEach(batchedUids -> {
            log.info("Deactivating registered learners in report service: {}", batchedUids);
            UpdateUserDetailsParams parameters = new UpdateUserDetailsParams(batchedUids);
            RequestEntity<UpdateUserDetailsParams> requestEntity = requestEntityFactory.createPutRequest(deactivateRegisteredLearnersUrl, parameters);
            UpdateUserResult response = httpClient.sendRequest(requestEntity, UpdateUserResult.class).getBody();
            totalResults.add(response);
        });
        log.info("Updated rows from deactivating registered learners: {}", totalResults.getAffectedRows());
        return totalResults;
    }

    public DeleteUserResults removeUserDetails(String uid) {
        log.info("Removing user details from report service: {}", uid);
        UpdateUserDetailsParams parameters = new UpdateUserDetailsParams(Collections.singletonList(uid));
        RequestEntity<UpdateUserDetailsParams> requestEntity = requestEntityFactory.createPutRequest(removeUserDataFromReportUrl, parameters);
        DeleteUserResults response = httpClient.sendRequest(requestEntity, DeleteUserResults.class).getBody();
        log.info("Response from remove user details request: {}", response);
        return response;
    }

}
