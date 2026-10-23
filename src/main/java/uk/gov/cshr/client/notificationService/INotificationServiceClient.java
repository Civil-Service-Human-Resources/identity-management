package uk.gov.cshr.client.notificationService;


import uk.gov.cshr.notifications.dto.BulkSendEmail;
import uk.gov.cshr.notifications.dto.BulkSendEmailResponse;
import uk.gov.cshr.notifications.dto.MessageDto;

public interface INotificationServiceClient {

    void sendEmail(String emailName, MessageDto body);

    BulkSendEmailResponse bulkSendEmails(BulkSendEmail emails);

}
