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
public class DeletionNotificationTask extends BaseTask {

    @Value("${accountPeriodsInMonths.notification}")
    private int notificationPeriodInMonths;

    private final IUtilService utilService;
    private final IdentityRepository identityRepository;
    private final IdentityManagementService identityManagementService;


    public DeletionNotificationTask(IUtilService utilService, IdentityRepository identityRepository, IdentityManagementService identityManagementService) {
        this.utilService = utilService;
        this.identityRepository = identityRepository;
        this.identityManagementService = identityManagementService;
    }

    @Override
    protected List<Identity> fetchUsers() {
        LocalDateTime deletionNotificationDate = utilService.getNowDateTime().minusMonths(notificationPeriodInMonths);
        List<Identity> identitiesToSendDeletionNotification = identityRepository.findForDeletionNotification(deletionNotificationDate);
        log.info("Number of inactive users for deletion notification who have logged-in/reactivated before deletion notification cutoff date {}: {}",
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
