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
        log.info("Deactivation cutoff date: {}", deactivationDateTime);

        log.debug("Fetching identities who have last logged-in before deactivation date");
        List<Identity> activeIdentitiesLastLoggedInBeforeDeactivationDate =
                identityRepository.findByActiveTrueAndLastLoggedInBefore(deactivationDateTime);
        int numberOfActiveIdentitiesLastLoggedInBeforeDeactivationDate
                = activeIdentitiesLastLoggedInBeforeDeactivationDate.size();
        log.info("Number of identities logged-in before deactivation cutoff date {}: {}",
                deactivationDateTime, numberOfActiveIdentitiesLastLoggedInBeforeDeactivationDate);

        log.debug("Fetching re-activations done after deactivation date");
        List<Reactivation> reactivationAfterDeactivationDate =
                reactivationRepository.findByReactivatedAtAfter(Date.from(deactivationDateTime));

        log.debug("Preparing emails list from the re-activations done after deactivation date");
        Set<String> reactivatedEmailsLowerCase = reactivationAfterDeactivationDate
                .stream()
                .map(r -> r.getEmail().toLowerCase())
                .collect(Collectors.toSet());
        log.debug("Number of emails reactivated after deactivation date: {}", reactivatedEmailsLowerCase.size());

        log.debug("Preparing identities list which are eligible for the deactivation");
        List<Identity> identitiesToBeDeactivate = activeIdentitiesLastLoggedInBeforeDeactivationDate
                .stream()
                .filter(i -> !reactivatedEmailsLowerCase.contains(i.getEmail().toLowerCase()))
                .collect(Collectors.toList());
        int numberOfIdentitiesToBeDeactivate = identitiesToBeDeactivate.size();
        log.info("Number of identities to be deactivated: {}", numberOfIdentitiesToBeDeactivate);

        log.info("Number of identities activated after deactivation cutoff date {} but did not login: {}",
                deactivationDateTime,
                numberOfActiveIdentitiesLastLoggedInBeforeDeactivationDate - numberOfIdentitiesToBeDeactivate);

        return identitiesToBeDeactivate;
    }

    @Override
    protected void updateUser(Identity user) {
        user.setActive(false);
        identityRepository.saveAndFlush(user);
        notificationService.send(messageService.createSuspensionMessage(user));
    }
}
