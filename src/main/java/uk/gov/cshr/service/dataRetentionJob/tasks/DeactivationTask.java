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
public class DeactivationTask extends BaseTask {

    @Value("${accountPeriodsInMonths.deactivation}")
    private int deactivationPeriodInMonths;

    private final IUtilService utilService;
    private final IdentityManagementService identityManagementService;
    private final IdentityRepository identityRepository;

    public DeactivationTask(IUtilService utilService, IdentityRepository identityRepository,
                            IdentityManagementService identityManagementService) {
        this.utilService = utilService;
        this.identityRepository = identityRepository;
        this.identityManagementService = identityManagementService;
    }

    @Override
    protected List<Identity> fetchUsers() {
        LocalDateTime deactivationDateTime = utilService.getNowDateTime().minusMonths(deactivationPeriodInMonths);
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
