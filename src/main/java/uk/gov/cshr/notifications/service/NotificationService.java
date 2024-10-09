package uk.gov.cshr.notifications.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.HttpClient;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.service.RequestEntityFactory;

@Service
public class NotificationService {

    private final HttpClient httpClient;

    private final RequestEntityFactory requestEntityFactory;

    private final String emailNotificationUrl;

    public NotificationService(HttpClient httpClient, RequestEntityFactory requestEntityFactory,
                               @Value("${notifications.email}") String emailNotificationUrl) {
        this.httpClient = httpClient;
        this.requestEntityFactory = requestEntityFactory;
        this.emailNotificationUrl = emailNotificationUrl;
    }

    public boolean send(MessageDto message) {
        RequestEntity<MessageDto> requestEntity = requestEntityFactory.createPostRequest(emailNotificationUrl, message);

        ResponseEntity<Void> response = httpClient.sendRequest(requestEntity, Void.class);

        return response.getStatusCode().is2xxSuccessful();
    }
}
