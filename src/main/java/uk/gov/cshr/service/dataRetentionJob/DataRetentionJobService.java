package uk.gov.cshr.service.dataRetentionJob;

import jdk.vm.ci.meta.Local;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.RequestEntityFactory;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeactivationTask;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeletionNotificationTask;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeletionTask;
import uk.gov.cshr.service.security.IdentityService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DataRetentionJobService {

    private final DeactivationTask deactivationTask;

    private final DeletionNotificationTask deletionNotificationTask;

    private final DeletionTask deletionTask;

    private final RequestEntityFactory requestEntityFactory;

    private final RestTemplate restTemplate;

    public DataRetentionJobService(RestTemplate restTemplate,
                                   RequestEntityFactory requestEntityFactory,
                                   DeactivationTask deactivationTask,
                                   DeletionNotificationTask deletionNotificationTask,
                                   DeletionTask deletionTask) {
        this.requestEntityFactory = requestEntityFactory;
        this.restTemplate = restTemplate;

        this.deactivationTask = deactivationTask;
        this.deletionNotificationTask = deletionNotificationTask;
        this.deletionTask = deletionTask;
    }

    public void runDataRetentionJob() {

        deletionTask.runTask();
        deletionNotificationTask.runTask();
        deactivationTask.runTask();

        if (restTemplate.exchange(requestEntityFactory.createLogoutRequest(), Void.class).getStatusCode().is2xxSuccessful()) {
            log.info("Management client user logged out after data retention execution");
        } else {
            log.error("Error logging out management client user after data retention execution, this may cause future executions to be unstable");
        }
    }


}
