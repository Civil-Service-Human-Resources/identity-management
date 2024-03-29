package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import uk.gov.cshr.domain.Identity;

import java.util.List;

@Slf4j
public abstract class BaseTask {

    protected abstract List<Identity> fetchUsers();

    protected abstract void updateUser(Identity user);

    public void runTask() {
        try {
            List<Identity> users = fetchUsers();
            if (users.size() > 0) {
                log.info(String.format("Processing %d accounts", users.size()));
                users.forEach(this::processUser);
            } else {
                log.info("Didn't find any users to process");
            }
        } catch (Exception e) {
            log.error("Error in task: %s", e);
        }
    }

    private void processUser(Identity user) {
        log.info(String.format("Processing user: %s (%s)", user.getEmail(), user.getUid()));
        try {
            this.updateUser(user);
        } catch (Exception e) {
            log.error(String.format("Failed to process user, exception: %s", e));
        }
    }
}
