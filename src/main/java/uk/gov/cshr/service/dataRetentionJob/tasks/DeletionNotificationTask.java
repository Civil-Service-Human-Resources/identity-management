package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;

@Slf4j
@Service
public class DeletionNotificationTask extends BaseTask {

    @Value("${accountPeriodsInMonths.notification}")
    private int notificationPeriodInMonths;

    private final IdentityRepository identityRepository;
    private final MessageService messageService;
    private final NotificationService notificationService;

    public DeletionNotificationTask(IdentityRepository identityRepository, MessageService messageService, NotificationService notificationService) {
        this.identityRepository = identityRepository;
        this.messageService = messageService;
        this.notificationService = notificationService;
    }

    @Override
    protected List<Identity> fetchUsers() {
        LocalDateTime deletionNotificationDate = LocalDateTime.now().minusMonths(notificationPeriodInMonths);
        List<Identity> identitiesToSendDeletionNotification = identityRepository.findByActiveFalseAndDeletionNotificationSentFalseAndLastLoggedInBefore(
                deletionNotificationDate.toInstant(UTC));
        log.info("Number of inactive users for deletion notification who have logged-in before deletion notification cutoff date {}: {}",
                deletionNotificationDate, identitiesToSendDeletionNotification.size());
        return identitiesToSendDeletionNotification;
    }

    @Override
    protected void updateUser(Identity user) {
        user.setDeletionNotificationSent(true);
        identityRepository.saveAndFlush(user);
        notificationService.send(messageService.createDeletionMessage(user));
    }
}
