package uk.gov.cshr.service.jobs.requiredLearningReminders.producer;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import uk.gov.cshr.service.jobs.BaseJobConfig;

import java.util.Collection;

@Getter
@ConfigurationProperties(prefix = "jobs.required-learning-notification.producer")
public class RequiredLearningReminderProducerJobConfiguration extends BaseJobConfig {

    @NotNull
    private final Collection<Integer> daysToRemind;

    public RequiredLearningReminderProducerJobConfiguration(String cron, boolean enabled, Collection<Integer> daysToRemind) {
        super(cron, enabled);
        this.daysToRemind = daysToRemind;
    }
}
