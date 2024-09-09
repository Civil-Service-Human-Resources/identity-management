package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.security.IdentityService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
public class DeletionTask extends BaseTask {

    @Value("${accountPeriodsInMonths.deletion}")
    private int deletionPeriodInMonths;

    private final IdentityRepository identityRepository;
    private final IdentityService identityService;
    private final MessageService messageService;
    private final NotificationService notificationService;

    public DeletionTask(IdentityRepository identityRepository, MessageService messageService,
                        NotificationService notificationService, IdentityService identityService) {
        this.identityRepository = identityRepository;
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.identityService = identityService;
    }

    @Override
    protected List<Identity> fetchUsers() {
        LocalDateTime deletionDate = LocalDateTime.now().minusMonths(deletionPeriodInMonths);
        log.info("Fetching inactive users for deletion who have logged-in before deletion cutoff date: {}", deletionDate);
        return identityRepository.findByActiveFalseAndLastLoggedInBefore(deletionDate.toInstant(ZoneOffset.UTC));
    }

    @Override
    protected void updateUser(Identity user) {
        log.info("deleting identity {} ", user.getEmail());
        identityService.deleteIdentity(user.getUid());
        notificationService.send(messageService.createDeletedMessage(user));
    }
}
