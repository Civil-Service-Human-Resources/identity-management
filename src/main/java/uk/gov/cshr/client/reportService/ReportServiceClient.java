package uk.gov.cshr.client.reportService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import uk.gov.cshr.client.IHttpClient;
import uk.gov.cshr.domain.DeleteUserResults;
import uk.gov.cshr.domain.UpdateUserDetailsParams;
import uk.gov.cshr.domain.UpdateUserResult;

@Slf4j
@Component
public class ReportServiceClient implements IReportServiceClient {

    private final IHttpClient httpClient;
    private final String deactivateRegisteredLearnersUrl;
    private final String removeUserDataFromReportUrl;

    public ReportServiceClient(@Qualifier("reportServiceHttpClient") IHttpClient httpClient,
                               @Value("${reporting.registeredLearners.deactivateUrl}") String deactivateRegisteredLearnersUrl,
                               @Value("${reporting.api.removeUserDataFromReportUrl}") String removeUserDataFromReportUrl) {
        this.httpClient = httpClient;
        this.deactivateRegisteredLearnersUrl = deactivateRegisteredLearnersUrl;
        this.removeUserDataFromReportUrl = removeUserDataFromReportUrl;
    }

    @Override
    public UpdateUserResult deactivateRegisteredLearners(UpdateUserDetailsParams params) {
        RequestEntity<UpdateUserDetailsParams> requestEntity = RequestEntity.put(deactivateRegisteredLearnersUrl).body(params);
        return httpClient.executeRequest(requestEntity, UpdateUserResult.class);
    }

    @Override
    public DeleteUserResults removeUserDetails(UpdateUserDetailsParams params) {
        RequestEntity<UpdateUserDetailsParams> requestEntity = RequestEntity.put(removeUserDataFromReportUrl).body(params);
        return httpClient.executeRequest(requestEntity, DeleteUserResults.class);
    }
}
