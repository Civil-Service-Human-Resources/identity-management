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
public class DeactivationTask extends BaseTask {

    @Value("${accountPeriodsInMonths.deactivation}")
    private int deactivationPeriodInMonths;

    private final IdentityRepository identityRepository;
    private final MessageService messageService;
    private final NotificationService notificationService;

    public DeactivationTask(IdentityRepository identityRepository, MessageService messageService, NotificationService notificationService) {
        this.identityRepository = identityRepository;
        this.messageService = messageService;
        this.notificationService = notificationService;
    }

    @Override
    protected List<Identity> fetchUsers() {
        LocalDateTime deactivationDate = LocalDateTime.now().minusMonths(deactivationPeriodInMonths);
        log.info("Fetching users for deactivation. Deactivation cutoff date: {}", deactivationDate);
        return identityRepository.findByActiveTrueAndLastLoggedInBefore(deactivationDate.toInstant(ZoneOffset.UTC));
    }

    @Override
    protected void updateUser(Identity user) {
        user.setActive(false);
        user.setAgencyTokenUid(null);
        identityRepository.saveAndFlush(user);
        notificationService.send(messageService.createSuspensionMessage(user));
    }
}
