package uk.gov.cshr.service.jobs.requiredLearningReminders.producer;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import uk.gov.cshr.service.jobs.BaseJobConfig;

import java.util.Collection;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "jobs.required-learning-notification.producer")
public class RequiredLearningReminderProducerJobConfiguration extends BaseJobConfig {

    @NotNull
    private Collection<Integer> daysToRemind;

}
