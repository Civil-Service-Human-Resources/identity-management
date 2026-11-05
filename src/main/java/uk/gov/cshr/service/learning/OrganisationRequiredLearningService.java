package uk.gov.cshr.service.learning;

import org.springframework.stereotype.Service;
import uk.gov.cshr.client.cslService.ICslServiceClient;
import uk.gov.cshr.client.cslService.models.RequiredLearningCourseMapResponse;
import uk.gov.cshr.domain.jobs.requiredLearningReminders.OrganisationUnitRequiredCourse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class OrganisationRequiredLearningService {

    private final ICslServiceClient cslServiceClient;

    public OrganisationRequiredLearningService(ICslServiceClient cslServiceClient) {
        this.cslServiceClient = cslServiceClient;
    }

    public Collection<OrganisationUnitRequiredCourse> fetchOrganisationsRequiredLearningForDueDate(LocalDate dueDate) {
        RequiredLearningCourseMapResponse map = cslServiceClient.getRequiredLearningMap(dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Collection<OrganisationUnitRequiredCourse> requiredCourses = new ArrayList<>();
        map.forEach((courseId, courseWithLearningPeriods) -> {
            String courseTitle = courseWithLearningPeriods.getTitle();
            courseWithLearningPeriods.getLearningPeriods().values()
                    .forEach(learningPeriodWithOrgs -> learningPeriodWithOrgs.getOrganisationIds()
                            .forEach(orgId -> requiredCourses.add(new OrganisationUnitRequiredCourse(orgId, courseId, courseTitle, learningPeriodWithOrgs.getLearningPeriodStartTimestamp(),
                                    learningPeriodWithOrgs.getLearningPeriodEnd()))));
        });
        return requiredCourses;
    }

}
