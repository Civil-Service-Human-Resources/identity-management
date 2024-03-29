package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

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
        log.info("Fetching users for deletion notification. Cutoff date: {}", deletionNotificationDate);
        return identityRepository.findByDeletionNotificationSentFalseAndLastLoggedInBefore(deletionNotificationDate.toInstant(ZoneOffset.UTC));
    }

    @Override
    protected void updateUser(Identity user) {
        user.setDeletionNotificationSent(true);
        identityRepository.saveAndFlush(user);
        notificationService.send(messageService.createDeletionMessage(user));
    }
}
