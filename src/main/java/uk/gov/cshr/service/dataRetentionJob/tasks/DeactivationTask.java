package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.ReactivationRepository;
import uk.gov.cshr.service.security.IdentityManagementService;

import java.time.Clock;
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

    private final IdentityManagementService identityManagementService;
    private final IdentityRepository identityRepository;
    private final ReactivationRepository reactivationRepository;

    public DeactivationTask(Clock clock, IdentityRepository identityRepository, ReactivationRepository reactivationRepository,
                            IdentityManagementService identityManagementService) {
        super(clock);
        this.identityRepository = identityRepository;
        this.reactivationRepository = reactivationRepository;
        this.identityManagementService = identityManagementService;
    }

    @Override
    protected List<Identity> fetchUsers() {
        Instant deactivationDateTime = now(clock).minusMonths(deactivationPeriodInMonths).toInstant(UTC);
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

        log.debug("Preparing identities list which are eligible for deactivation");
        List<Identity> identitiesToBeDeactivated = activeIdentitiesLastLoggedInBeforeDeactivationDate
                .stream()
                .filter(i -> !reactivatedEmailsLowerCase.contains(i.getEmail().toLowerCase()))
                .collect(Collectors.toList());
        int numberOfIdentitiesToBeDeactivated = identitiesToBeDeactivated.size();

        log.info("Number of identities activated but did not log in after deactivation cutoff date {}: {}",
                deactivationDateTime,
                numberOfActiveIdentitiesLastLoggedInBeforeDeactivationDate - numberOfIdentitiesToBeDeactivated);

        log.info("Number of identities to be deactivated who have not logged-in or not activated since the deactivation cutoff date {}: {}",
                deactivationDateTime, numberOfIdentitiesToBeDeactivated);

        return identitiesToBeDeactivated;
    }

    @Override
    protected void updateUsers(List<Identity> users) {
        identityManagementService.deactivateIdentities(users);
    }

    @Override
    protected String getTaskName() {
        return "deactivate users";
    }
}
