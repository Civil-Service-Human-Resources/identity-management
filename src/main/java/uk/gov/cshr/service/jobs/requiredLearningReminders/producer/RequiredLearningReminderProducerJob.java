package uk.gov.cshr.service.jobs.requiredLearningReminders.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.jobs.JobType;
import uk.gov.cshr.job.entity.JobExecution;
import uk.gov.cshr.service.jobs.ConfiguredJob;
import uk.gov.cshr.utils.IUtilService;

import java.time.LocalDate;

@Service
@Slf4j
public class RequiredLearningReminderProducerJob extends ConfiguredJob<RequiredLearningReminderProducerJobConfiguration> {

    private final OrganisationReminderService organisationReminderService;
    private final IUtilService utilService;

    public RequiredLearningReminderProducerJob(RequiredLearningReminderProducerJobConfiguration config, OrganisationReminderService organisationReminderService, IUtilService utilService) {
        super(config);
        this.organisationReminderService = organisationReminderService;
        this.utilService = utilService;
    }

    @Override
    public JobType getJobType() {
        return JobType.REQUIRED_LEARNING_NOTIFICATION_PRODUCER;
    }

    public void run(JobExecution jobExecution) {
        config.getDaysToRemind().forEach(daysToRemind -> {
            LocalDate dueDate = utilService.getNowDate().plusDays(daysToRemind);
            organisationReminderService.fetchAndStoreOrganisationReminderActions(jobExecution, dueDate);
        });
    }
}
