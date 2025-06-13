package uk.gov.cshr.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.HttpClient;
import uk.gov.cshr.exceptions.NotificationException;
import uk.gov.cshr.notifications.dto.BulkSendEmail;
import uk.gov.cshr.notifications.dto.BulkSendEmailResponse;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.service.RequestEntityFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class NotificationService {

    private final HttpClient httpClient;

    private final RequestEntityFactory requestEntityFactory;

    private final String bulkEmailNotificationUrl;
    private final Integer bulkBatchSize;

    public NotificationService(HttpClient httpClient, RequestEntityFactory requestEntityFactory,
                               @Value("${notifications.bulkEmail}") String bulkEmailNotificationUrl,
                                @Value("${notifications.bulkBatchSize}") Integer bulkBatchSize) {
        this.httpClient = httpClient;
        this.requestEntityFactory = requestEntityFactory;
        this.bulkEmailNotificationUrl = bulkEmailNotificationUrl;
        this.bulkBatchSize = bulkBatchSize;
    }

    public boolean send(MessageDto message) {
        return send(Collections.singletonList(message)).getSuccessfulEmailRefs().size() == 1;
    }

    public BulkSendEmailResponse send(List<MessageDto> messages) {
        BulkSendEmailResponse response = new BulkSendEmailResponse(new ArrayList<>(), new ArrayList<>());
        List<List<MessageDto>> batches = IntStream.iterate(0, i -> i + bulkBatchSize)
                .limit((int) Math.ceil((double) messages.size() / bulkBatchSize))
                .mapToObj(i -> messages.subList(i, Math.min(i + bulkBatchSize, messages.size())))
                .collect(Collectors.toList());
        for (List<MessageDto> batch : batches) {
            BulkSendEmail body = new BulkSendEmail(batch);
            RequestEntity<BulkSendEmail> requestEntity = requestEntityFactory.createPostRequest(bulkEmailNotificationUrl, body);
            BulkSendEmailResponse batchResponse = httpClient.sendRequest(requestEntity, BulkSendEmailResponse.class).getBody();
            response.getFailedEmails().addAll(batchResponse.getFailedEmails());
            response.getSuccessfulEmailRefs().addAll(batchResponse.getSuccessfulEmailRefs());
        }
        if (!response.getFailedEmails().isEmpty()) {
            response.getFailedEmails().forEach(failedEmail -> {
                log.error("Email {} failed to send. Reason: {}", failedEmail.getResource(), failedEmail.getReason());
            });
            throw new NotificationException(String.format("%s emails failed to send", response.getFailedEmails().size()));
        }
        return response;
    }
}
