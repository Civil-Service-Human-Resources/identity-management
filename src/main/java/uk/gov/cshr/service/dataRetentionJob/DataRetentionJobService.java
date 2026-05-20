package uk.gov.cshr.service.dataRetentionJob;

import lombok.extern.slf4j.Slf4j;
import uk.gov.cshr.service.dataRetentionJob.tasks.BaseTask;

import java.util.LinkedList;

@Slf4j
public class DataRetentionJobService {

    private final LinkedList<BaseTask> tasks;

    public DataRetentionJobService(LinkedList<BaseTask> tasks) {
        this.tasks = tasks;
    }

    public void runDataRetentionJob() {
        for (BaseTask task : tasks) {
            task.runTask();
        }
    }


}
