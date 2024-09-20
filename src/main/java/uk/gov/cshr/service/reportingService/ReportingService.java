package uk.gov.cshr.service.reportingService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.HttpClient;
import uk.gov.cshr.domain.RemoveUserDetailsParams;
import uk.gov.cshr.service.RequestEntityFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

@Service
@Slf4j
public class ReportingService {
    private final HttpClient httpClient;
    private final RequestEntityFactory requestEntityFactory;
    private final String removeUserDetailsUrl;
    private final String removeUserDataFromReportUrl;

    public ReportingService(
            HttpClient httpClient,
            RequestEntityFactory requestEntityFactory,
            @Value("${reporting.removeUserDataUrl}") String removeUserDetailsUrl,
            @Value("${reporting.removeUserDataFromReportUrl}") String removeUserDataFromReportUrl
    ) {
        this.httpClient = httpClient;
        this.requestEntityFactory = requestEntityFactory;
        this.removeUserDetailsUrl = removeUserDetailsUrl;
        this.removeUserDataFromReportUrl = removeUserDataFromReportUrl;
    }

    public ResponseEntity<Void> removeUserDetails(List<String> uids) {
        log.debug("ReportingService:removeUserDetails: uids: {}", uids);
        RemoveUserDetailsParams parameters = new RemoveUserDetailsParams(uids);
        RequestEntity<RemoveUserDetailsParams> requestEntity = requestEntityFactory.createPutRequest(removeUserDetailsUrl, parameters);
        return httpClient.sendRequest(requestEntity, Void.class);
    }

    public ResponseEntity<Void> removeUserDetailsDataRetentionJob(List<String> uids) {
        log.debug("ReportingService:removeUserDetailsDataRetentionJob: uids: {}", uids);
        RemoveUserDetailsParams parameters = new RemoveUserDetailsParams(uids);
        RequestEntity<RemoveUserDetailsParams> requestEntity = requestEntityFactory.createPutRequest(removeUserDataFromReportUrl, parameters);
        return httpClient.sendRequest(requestEntity, Void.class);
    }

    public ResponseEntity<Void> removeUserDetails(String uid) {
        List<String> uidList = new ArrayList<>(singletonList(uid));
        return removeUserDetails(uidList);
    }

    public ResponseEntity<Void> removeUserDetailsByDataRetentionJob(String uid) {
        List<String> uidList = new ArrayList<>(singletonList(uid));
        return removeUserDetailsDataRetentionJob(uidList);
    }
}
