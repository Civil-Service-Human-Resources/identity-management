package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.identity.entity.Identity;
import uk.gov.cshr.identity.repository.IdentityRepository;
import uk.gov.cshr.service.security.IdentityManagementService;
import uk.gov.cshr.utils.IUtilService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DeletionTask extends LoopingTask {

    @Value("${accountPeriodsInMonths.deletion}")
    private int deletionPeriodInMonths;

    private final IUtilService utilService;
    private final IdentityRepository identityRepository;
    private final IdentityManagementService identityManagementService;

    public DeletionTask(IUtilService utilService, IdentityRepository identityRepository,
                        IdentityManagementService identityManagementService) {
        this.utilService = utilService;
        this.identityRepository = identityRepository;
        this.identityManagementService = identityManagementService;
    }

    @Override
    protected List<Identity> fetchUsers() {
        LocalDateTime deletionDate = utilService.getNowDateTime().minusMonths(deletionPeriodInMonths);
        List<Identity> identitiesToBeDeleted = identityRepository.findForDeletion(deletionDate);
        log.info("Number of inactive users for deletion who have logged-in or reactivated before deletion cutoff date {}: {}",
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
