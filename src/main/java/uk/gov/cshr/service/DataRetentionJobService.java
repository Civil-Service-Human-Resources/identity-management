package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.security.IdentityService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
public class DataRetentionJobService {

    @Value("${accountPeriodsInMonths.deactivation}")
    private int deactivationPeriodInMonths;

    @Value("${accountPeriodsInMonths.notification}")
    private int notificationPeriodInMonths;

    @Value("${accountPeriodsInMonths.deletion}")
    private int deletionPeriodInMonths;

    private final IdentityRepository identityRepository;

    private final IdentityService identityService;

    private final NotificationService notificationService;

    private final MessageService messageService;

    private final RequestEntityFactory requestEntityFactory;

    private final RestTemplate restTemplate;

    public DataRetentionJobService(IdentityRepository identityRepository,
                                   IdentityService identityService,
                                   NotificationService notificationService,
                                   MessageService messageService,
                                   RestTemplate restTemplate,
                                   RequestEntityFactory requestEntityFactory) {
        this.identityRepository = identityRepository;
        this.identityService = identityService;
        this.notificationService = notificationService;
        this.messageService = messageService;
        this.requestEntityFactory = requestEntityFactory;
        this.restTemplate = restTemplate;
    }

    public void runDataRetentionJob() {
        // First, delete users who haven't logged in for more than 26 months
        deleteUsersDataRetention();

        // Next, remind users who haven't logged in for more than 24 months that they will be deleted in 2m months
        notifyUsersOfDeletionDataRetention();

        // Finally, deactivate users who haven't logged in for more than 13 months
        deactivateUsersDataRetention();

        if (restTemplate.exchange(requestEntityFactory.createLogoutRequest(), Void.class).getStatusCode().is2xxSuccessful()) {
            log.info("Management client user logged out after data retention execution");
        } else {
            log.error("Error logging out management client user after data retention execution, this may cause future executions to be unstable");
        }
    }

    private void deactivateUser(Identity user) {
        try {
            log.info("deactivating identity {} ", user.getEmail());
            user.setActive(false);
            user.setAgencyTokenUid(null);
            identityRepository.saveAndFlush(user);
            notificationService.send(messageService.createSuspensionMessage(user));
        } catch(Exception e) {
            log.error(String.format("Failed to deactivate user %s (%s). Error: %s", user.getUid(), user.getEmail(), e));
        }
    }

    private void deactivateUsersDataRetention() {
        try {
            LocalDateTime deactivationDate = LocalDateTime.now().minusMonths(deactivationPeriodInMonths);
            log.info("deactivation date {}", deactivationDate);
            Iterable<Identity> identities = identityRepository.findByActiveTrueAndLastLoggedInBefore(deactivationDate.toInstant(ZoneOffset.UTC));
            identities.forEach(this::deactivateUser);
        } catch(Exception e) {
            log.error(String.format("Deactivate users job failed: %s", e));
        }
    }

    private void notifyUserOfDeletion(Identity user) {
        try {
            log.info("sending notify {} ", user.getEmail());
            user.setDeletionNotificationSent(true);
            identityRepository.saveAndFlush(user);
            notificationService.send(messageService.createDeletionMessage(user));
        } catch(Exception e) {
            log.error(String.format("Failed to notify user %s (%s) of deletion. Error: %s", user.getUid(), user.getEmail(), e));
        }
    }

    private void notifyUsersOfDeletionDataRetention() {
        try {
            LocalDateTime deletionNotificationDate = LocalDateTime.now().minusMonths(notificationPeriodInMonths);
            log.info("deleteNotifyDate {}", deletionNotificationDate);
            Iterable<Identity> identities = identityRepository.findByDeletionNotificationSentFalseAndLastLoggedInBefore(deletionNotificationDate.toInstant(ZoneOffset.UTC));
            identities.forEach(this::notifyUserOfDeletion);
        } catch(Exception e) {
            log.error(String.format("Delete notification job failed: %s", e));
        }
    }

    private void deleteUser(Identity user) {
        try {
            log.info("deleting identity {} ", user.getEmail());
            identityService.deleteIdentity(user.getUid());
            notificationService.send(messageService.createDeletedMessage(user));
        } catch(Exception e) {
            log.error(String.format("Failed to delete user %s (%s). Error: %s", user.getUid(), user.getEmail(), e));
        }
    }

    private void deleteUsersDataRetention() {
        try {
            LocalDateTime deletionDate = LocalDateTime.now().minusMonths(deletionPeriodInMonths);
            log.info("deleteDate {}", deletionDate);
            Iterable<Identity> identities = identityRepository.findByLastLoggedInBefore(deletionDate.toInstant(ZoneOffset.UTC));
            identities.forEach(this::deleteUser);
        } catch(Exception e) {
            log.error(String.format("Delete user job failed: %s", e));
        }
    }
}
