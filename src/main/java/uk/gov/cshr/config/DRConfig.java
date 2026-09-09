package uk.gov.cshr.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.cshr.service.dataRetentionJob.DataRetentionJobService;
import uk.gov.cshr.service.dataRetentionJob.tasks.BaseTask;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeactivationTask;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeletionNotificationTask;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeletionTask;

import java.util.LinkedList;

@Configuration
@RequiredArgsConstructor
public class DRConfig {

    private final DeletionTask deletionTask;
    private final DeletionNotificationTask deletionNotificationTask;
    private final DeactivationTask deactivationTask;

    @Bean
    public DataRetentionJobService service() {
        LinkedList<BaseTask> tasks = new LinkedList<>();
        tasks.add(deletionTask);
        tasks.add(deletionNotificationTask);
        tasks.add(deactivationTask);
        return new DataRetentionJobService(tasks);
    }

}
