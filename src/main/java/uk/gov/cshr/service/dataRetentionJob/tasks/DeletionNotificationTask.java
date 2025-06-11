package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.security.IdentityManagementService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;

@Slf4j
@Service
public class DeletionNotificationTask extends BaseTask {

    @Value("${accountPeriodsInMonths.notification}")
    private int notificationPeriodInMonths;

    private final IdentityRepository identityRepository;
    private final IdentityManagementService identityManagementService;


    public DeletionNotificationTask(Clock clock, IdentityRepository identityRepository, IdentityManagementService identityManagementService) {
        super(clock);
        this.identityRepository = identityRepository;
        this.identityManagementService = identityManagementService;
    }

    @Override
    protected List<Identity> fetchUsers() {
        LocalDateTime deletionNotificationDate = LocalDateTime.now(clock).minusMonths(notificationPeriodInMonths);
        List<Identity> identitiesToSendDeletionNotification = identityRepository.findByActiveFalseAndDeletionNotificationSentFalseAndLastLoggedInBefore(
                deletionNotificationDate.toInstant(UTC));
        log.info("Number of inactive users for deletion notification who have logged-in before deletion notification cutoff date {}: {}",
                deletionNotificationDate, identitiesToSendDeletionNotification.size());
        return identitiesToSendDeletionNotification;
    }

    @Override
    protected void updateUsers(List<Identity> users) {
        identityManagementService.markUsersForDeletion(users);
    }

    @Override
    protected String getTaskName() {
        return "deletion notification";
    }
}
