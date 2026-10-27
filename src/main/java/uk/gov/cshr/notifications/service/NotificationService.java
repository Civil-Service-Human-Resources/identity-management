package uk.gov.cshr.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.notificationService.INotificationServiceClient;
import uk.gov.cshr.exceptions.NotificationException;
import uk.gov.cshr.notifications.dto.BulkSendEmail;
import uk.gov.cshr.notifications.dto.BulkSendEmailResponse;
import uk.gov.cshr.notifications.dto.MessageDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Slf4j
public class NotificationService {

    private final INotificationServiceClient client;
    private final Integer bulkBatchSize;

    public NotificationService(INotificationServiceClient client,
                               @Value("${notifications.bulkBatchSize}") Integer bulkBatchSize) {
        this.client = client;
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
                .toList();
        for (List<MessageDto> batch : batches) {
            BulkSendEmail body = new BulkSendEmail(batch);
            BulkSendEmailResponse batchResponse = client.bulkSendEmails(body);
            response.getFailedEmails().addAll(batchResponse.getFailedEmails());
            response.getSuccessfulEmailRefs().addAll(batchResponse.getSuccessfulEmailRefs());
        }
        if (!response.getFailedEmails().isEmpty()) {
            response.getFailedEmails().forEach(failedEmail -> log.error("Email {} failed to send. Reason: {}", failedEmail.getResource(), failedEmail.getReason()));
            throw new NotificationException(String.format("%s emails failed to send", response.getFailedEmails().size()));
        }
        return response;
    }
}
