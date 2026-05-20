package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.security.IdentityManagementService;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;

@Slf4j
@Service
public class DeactivationTask extends BaseTask {

    @Value("${accountPeriodsInMonths.deactivation}")
    private int deactivationPeriodInMonths;

    private final IdentityManagementService identityManagementService;
    private final IdentityRepository identityRepository;

    public DeactivationTask(Clock clock, IdentityRepository identityRepository,
                            IdentityManagementService identityManagementService) {
        super(clock);
        this.identityRepository = identityRepository;
        this.identityManagementService = identityManagementService;
    }

    @Override
    protected List<Identity> fetchUsers() {
        Instant deactivationDateTime = now(clock).minusMonths(deactivationPeriodInMonths).toInstant(UTC);
        List<Identity> identitiesToBeDeactivated = identityRepository.findForDeactivation(deactivationDateTime);
        log.info("Number of active users for deactivation who have logged-in or reactivated before cutoff date {}: {}",
                deactivationDateTime, identitiesToBeDeactivated.size());
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
