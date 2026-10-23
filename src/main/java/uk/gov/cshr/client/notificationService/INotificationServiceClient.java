package uk.gov.cshr.client.notificationService;


import uk.gov.cshr.notifications.dto.BulkSendEmail;
import uk.gov.cshr.notifications.dto.BulkSendEmailResponse;

public interface INotificationServiceClient {

    BulkSendEmailResponse bulkSendEmails(BulkSendEmail emails);

}
