package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.ReactivationRepository;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;

@Slf4j
@Service
public class DeactivationTask extends BaseTask {

    @Value("${accountPeriodsInMonths.deactivation}")
    private int deactivationPeriodInMonths;

    private final IdentityRepository identityRepository;
    private final MessageService messageService;
    private final NotificationService notificationService;
    private final ReactivationRepository reactivationRepository;

    public DeactivationTask(IdentityRepository identityRepository, MessageService messageService,
                            NotificationService notificationService, ReactivationRepository reactivationRepository) {
        this.identityRepository = identityRepository;
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.reactivationRepository = reactivationRepository;
    }

    @Override
    protected List<Identity> fetchUsers() {

        Instant deactivationDateTime = now().minusMonths(deactivationPeriodInMonths).toInstant(UTC);
        log.info("DeactivationTask: Deactivation cutoff date: {}", deactivationDateTime);

        log.info("DeactivationTask: Fetching identities who have last logged-in before deactivation date");
        List<Identity> activeIdentitiesLastLoggedInBeforeDeactivationDate =
                identityRepository.findByActiveTrueAndLastLoggedInBefore(deactivationDateTime);
        log.info("DeactivationTask: Identities who have last logged-in before deactivation date: {}",
                activeIdentitiesLastLoggedInBeforeDeactivationDate);

        int numberOfActiveIdentitiesLastLoggedInBeforeDeactivationDate
                = activeIdentitiesLastLoggedInBeforeDeactivationDate.size();
        log.info("DeactivationTask: Number of identities logged-in before deactivation cutoff date {}: {}",
                deactivationDateTime, numberOfActiveIdentitiesLastLoggedInBeforeDeactivationDate);

        log.info("DeactivationTask: Fetching re-activations done after deactivation date");
        List<Reactivation> reactivationAfterDeactivationDate =
                reactivationRepository.findByReactivatedAtAfter(Date.from(deactivationDateTime));
        log.info("DeactivationTask: Re-activations done after deactivation date: {}", reactivationAfterDeactivationDate);

        log.info("DeactivationTask: Preparing emails list from the re-activations done after deactivation date");
        Set<String> reactivatedEmailsLowerCase = reactivationAfterDeactivationDate
                .stream()
                .map(r -> r.getEmail().toLowerCase())
                .collect(Collectors.toSet());
        log.info("DeactivationTask: Emails list from the re-activations done after deactivation date: {}", reactivatedEmailsLowerCase);

        log.info("DeactivationTask: Preparing identities list which are eligible for the deactivation");
        List<Identity> identitiesToBeDeactivate = activeIdentitiesLastLoggedInBeforeDeactivationDate
                .stream()
                .filter(i -> !reactivatedEmailsLowerCase.contains(i.getEmail().toLowerCase()))
                .collect(Collectors.toList());
        log.info("DeactivationTask: Identities list which are eligible for the deactivation: {}", identitiesToBeDeactivate);

        int numberOfIdentitiesToBeDeactivate = identitiesToBeDeactivate.size();
        log.info("DeactivationTask: Number of identities activated after deactivation cutoff date {} but did not login: {}",
                deactivationDateTime,
                numberOfActiveIdentitiesLastLoggedInBeforeDeactivationDate - numberOfIdentitiesToBeDeactivate);
        log.info("DeactivationTask: Number of identities to be deactivated: {}", numberOfIdentitiesToBeDeactivate);

        return identitiesToBeDeactivate;
    }

    @Override
    protected void updateUser(Identity user) {
        user.setActive(false);
        identityRepository.saveAndFlush(user);
        notificationService.send(messageService.createSuspensionMessage(user));
    }
}
