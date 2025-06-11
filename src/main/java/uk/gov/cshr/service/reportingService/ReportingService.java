package uk.gov.cshr.service.reportingService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.HttpClient;
import uk.gov.cshr.domain.UpdateUserDetailsParams;
import uk.gov.cshr.domain.UpdateUserResult;
import uk.gov.cshr.service.RequestEntityFactory;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ReportingService {
    private final HttpClient httpClient;
    private final RequestEntityFactory requestEntityFactory;
    private final String deactivateRegisteredLearnersUrl;
    private final String removeUserDataFromReportUrl;

    public ReportingService(
            HttpClient httpClient,
            RequestEntityFactory requestEntityFactory,
            @Value("${reporting.registeredLearners.deactivateUrl}") String deactivateRegisteredLearnersUrl,
            @Value("${reporting.api.removeUserDataFromReportUrl}") String removeUserDataFromReportUrl
    ) {
        this.httpClient = httpClient;
        this.requestEntityFactory = requestEntityFactory;
        this.deactivateRegisteredLearnersUrl = deactivateRegisteredLearnersUrl;
        this.removeUserDataFromReportUrl = removeUserDataFromReportUrl;
    }

    public ResponseEntity<UpdateUserResult> deactivateRegisteredLearners(List<String> uids) {
        UpdateUserDetailsParams parameters = new UpdateUserDetailsParams(uids);
        RequestEntity<UpdateUserDetailsParams> requestEntity = requestEntityFactory.createPutRequest(deactivateRegisteredLearnersUrl, parameters);
        return httpClient.sendRequest(requestEntity, UpdateUserResult.class);
    }

    public ResponseEntity<Void> removeUserDetails(String uid) {
        log.info("Removing user details from report service: {}", uid);
        UpdateUserDetailsParams parameters = new UpdateUserDetailsParams(Collections.singletonList(uid));
        RequestEntity<UpdateUserDetailsParams> requestEntity = requestEntityFactory.createPutRequest(removeUserDataFromReportUrl, parameters);
        return httpClient.sendRequest(requestEntity, Void.class);
    }

}
