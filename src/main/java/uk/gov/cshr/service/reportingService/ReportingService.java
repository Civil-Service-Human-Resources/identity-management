package uk.gov.cshr.service.reportingService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.HttpClient;
import uk.gov.cshr.service.RequestEntityFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ReportingService {
    private final HttpClient httpClient;
    private final RequestEntityFactory requestEntityFactory;
    private final String removeUserDetailsUrl;

    public ReportingService(
            HttpClient httpClient,
            RequestEntityFactory requestEntityFactory,
            @Value("${reporting.removeUserDataUrl}") String removeUserDetailsUrl
    ) {
        this.httpClient = httpClient;
        this.requestEntityFactory = requestEntityFactory;
        this.removeUserDetailsUrl = removeUserDetailsUrl;
    }

    public ResponseEntity<Void> removeUserDetails(List<String> uids){
        RequestEntity<Void> requestEntity = requestEntityFactory.createPutRequest(removeUserDetailsUrl, uids);
        return httpClient.sendRequest(requestEntity);
    }

    public ResponseEntity<Void> removeUserDetails(String uid){
        List<String> uidList = new ArrayList<>(Arrays.asList(uid));
        return removeUserDetails(uidList);
    }


}
