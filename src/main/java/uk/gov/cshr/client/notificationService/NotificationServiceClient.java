package uk.gov.cshr.client.notificationService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import uk.gov.cshr.client.IHttpClient;
import uk.gov.cshr.notifications.dto.BulkSendEmail;
import uk.gov.cshr.notifications.dto.BulkSendEmailResponse;
import uk.gov.cshr.notifications.dto.MessageDto;

@Component
@Slf4j
public class NotificationServiceClient implements INotificationServiceClient {

    @Value("${notifications.email}")
    private String emails;
    @Value("${notifications.bulkEmail}")
    private String bulkEmails;
    private final IHttpClient httpClient;

    public NotificationServiceClient(@Qualifier("notificationServiceHttpClient") IHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void sendEmail(String emailName, MessageDto body) {
        log.info("Sending email '{}'", emailName);
        String url = String.format("%s/%s/send", emails, emailName);
        RequestEntity<MessageDto> request = RequestEntity.post(url).body(body);
        httpClient.executeRequest(request, Void.class);
    }

    @Override
    public BulkSendEmailResponse bulkSendEmails(BulkSendEmail emails) {
        RequestEntity<BulkSendEmail> request = RequestEntity.post(bulkEmails).body(emails);
        return httpClient.executeRequest(request, BulkSendEmailResponse.class);
    }
}
