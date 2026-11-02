package uk.gov.cshr.client.notificationService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import uk.gov.cshr.client.IHttpClient;
import uk.gov.cshr.notifications.dto.BulkSendEmail;
import uk.gov.cshr.notifications.dto.BulkSendEmailResponse;

@Component
@Slf4j
public class NotificationServiceClient implements INotificationServiceClient {
    
    @Value("${notifications.bulkEmail}")
    private String bulkEmails;
    private final IHttpClient httpClient;

    public NotificationServiceClient(@Qualifier("notificationServiceHttpClient") IHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public BulkSendEmailResponse bulkSendEmails(BulkSendEmail emails) {
        RequestEntity<BulkSendEmail> request = RequestEntity.post(bulkEmails).body(emails);
        return httpClient.executeRequest(request, BulkSendEmailResponse.class);
    }
}
