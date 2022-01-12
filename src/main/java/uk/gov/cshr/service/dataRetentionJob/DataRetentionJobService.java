package uk.gov.cshr.service.dataRetentionJob;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.service.RequestEntityFactory;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeactivationTask;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeletionNotificationTask;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeletionTask;

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

        log.info("Running delete users task");
        deletionTask.runTask();

        log.info("Running deletion notification task");
        deletionNotificationTask.runTask();

        log.info("Running deactivate users task");
        deactivationTask.runTask();

        if (restTemplate.exchange(requestEntityFactory.createLogoutRequest(), Void.class).getStatusCode().is2xxSuccessful()) {
            log.info("Management client user logged out after data retention execution");
        } else {
            log.error("Error logging out management client user after data retention execution, this may cause future executions to be unstable");
        }
    }


}
