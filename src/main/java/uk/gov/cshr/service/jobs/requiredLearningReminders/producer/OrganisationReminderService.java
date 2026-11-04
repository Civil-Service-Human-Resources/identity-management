package uk.gov.cshr.service.jobs.requiredLearningReminders.producer;

import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.jobs.requiredLearningReminders.OrganisationUnitRequiredCourse;
import uk.gov.cshr.job.entity.JobExecution;
import uk.gov.cshr.job.entity.requiredLearningReminders.RequiredLearningReminderOrganisationUnitAction;
import uk.gov.cshr.job.repository.requiredLearningReminders.RequiredLearningReminderOrganisationUnitActionRepository;
import uk.gov.cshr.service.learning.OrganisationRequiredLearningService;

import java.time.LocalDate;
import java.util.Collection;

@Service
public class OrganisationReminderService {

    private final RequiredLearningReminderOrganisationUnitActionRepository organisationUnitActionRepository;
    private final OrganisationRequiredLearningService requiredLearningReminderService;

    public OrganisationReminderService(RequiredLearningReminderOrganisationUnitActionRepository organisationUnitActionRepository, OrganisationRequiredLearningService requiredLearningReminderService) {
        this.organisationUnitActionRepository = organisationUnitActionRepository;
        this.requiredLearningReminderService = requiredLearningReminderService;
    }

    public Collection<RequiredLearningReminderOrganisationUnitAction> fetchAndStoreOrganisationReminderActions(JobExecution job, LocalDate dueDate) {
        Collection<OrganisationUnitRequiredCourse> requiredCourses = requiredLearningReminderService.fetchOrganisationsRequiredLearningForDueDate(dueDate);
        Collection<RequiredLearningReminderOrganisationUnitAction> actions = requiredCourses
                .stream().map(requiredCourse -> new RequiredLearningReminderOrganisationUnitAction(job, requiredCourse.getOrganisationId(),
                        requiredCourse.getCourseId(), requiredCourse.getCourseTitle(), requiredCourse.getLearningPeriodStart(),
                        requiredCourse.getLearningPeriodEnd())).toList();
        organisationUnitActionRepository.saveAll(actions);
        return actions;
    }
}
