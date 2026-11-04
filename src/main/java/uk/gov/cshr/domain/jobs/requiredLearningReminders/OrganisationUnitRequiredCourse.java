package uk.gov.cshr.domain.jobs.requiredLearningReminders;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class OrganisationUnitRequiredCourse {
    private Integer organisationId;
    private String courseId;
    private String courseTitle;
    private LocalDateTime learningPeriodStart;
    private LocalDateTime learningPeriodEnd;
    ;
}
