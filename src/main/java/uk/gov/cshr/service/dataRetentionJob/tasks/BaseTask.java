package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import uk.gov.cshr.domain.Identity;

import java.time.Clock;
import java.util.List;

@Slf4j
public abstract class BaseTask {

    protected final Clock clock;

    protected BaseTask(Clock clock) {
        this.clock = clock;
    }

    protected abstract List<Identity> fetchUsers();

    protected abstract void updateUsers(List<Identity> users);

    protected abstract String getTaskName();

    public void runTask() {
        try {
            log.info("Running {} task", getTaskName());
            List<Identity> users = fetchUsers();
            processUsers(users);
        } catch (Exception e) {
            log.error(String.format("Error in %s task: %s", getTaskName(), e));
        }
    }

    public void processUsers(List<Identity> users) {
        if (!users.isEmpty()) {
            log.info(String.format("Processing %d accounts", users.size()));
            updateUsers(users);
        } else {
            log.info("Didn't find any users to process");
        }
    }

}
