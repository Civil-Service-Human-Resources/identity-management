package uk.gov.cshr.service.dataRetentionJob;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.service.dataRetentionJob.tasks.BaseTask;

import java.util.List;

@Service
@Slf4j
public class DataRetentionJobService {

    private final List<BaseTask> tasks;

    public DataRetentionJobService(List<BaseTask> tasks) {
        this.tasks = tasks;
    }

    public void runDataRetentionJob() {
        for (BaseTask task : tasks) {
            task.runTask();
        }
    }


}
