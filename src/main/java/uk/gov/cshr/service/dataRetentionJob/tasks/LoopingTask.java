package uk.gov.cshr.service.dataRetentionJob.tasks;

import lombok.extern.slf4j.Slf4j;
import uk.gov.cshr.domain.Identity;

import java.time.Clock;
import java.util.List;

@Slf4j
public abstract class LoopingTask extends BaseTask {

    protected LoopingTask(Clock clock) {
        super(clock);
    }

    protected abstract void updateUser(Identity user);

    @Override
    protected void updateUsers(List<Identity> users) {
        for (Identity user : users) {
            log.info(String.format("Processing user: %s (%s)", user.getEmail(), user.getUid()));
            try {
                this.updateUser(user);
            } catch (Exception e) {
                log.error(String.format("Failed to process user, exception: %s", e));
            }
        }
    }
}
