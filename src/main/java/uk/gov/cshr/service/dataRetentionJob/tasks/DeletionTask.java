package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.security.IdentityManagementService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
public class DeletionTask extends LoopingTask {

    @Value("${accountPeriodsInMonths.deletion}")
    private int deletionPeriodInMonths;

    private final IdentityRepository identityRepository;
    private final IdentityManagementService identityManagementService;

    public DeletionTask(Clock clock, IdentityRepository identityRepository,
                        IdentityManagementService identityManagementService) {
        super(clock);
        this.identityRepository = identityRepository;
        this.identityManagementService = identityManagementService;
    }

    @Override
    protected List<Identity> fetchUsers() {
        LocalDateTime deletionDate = LocalDateTime.now(clock).minusMonths(deletionPeriodInMonths);
        List<Identity> identitiesToBeDeleted = identityRepository.findByActiveFalseAndLastLoggedInBefore(deletionDate.toInstant(ZoneOffset.UTC));
        log.info("Number of inactive users for deletion who have logged-in before deletion cutoff date {}: {}",
                deletionDate, identitiesToBeDeleted.size());
        return identitiesToBeDeleted;
    }

    @Override
    protected String getTaskName() {
        return "delete users";
    }

    @Override
    protected void updateUser(Identity user) {
        identityManagementService.deleteIdentity(user);
    }
}
