package uk.gov.cshr.integration.jobs.requiredLerningReminder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.integration.IntegrationTestBase;
import uk.gov.cshr.job.entity.requiredLearningReminders.RequiredLearningReminderOrganisationUnitAction;
import uk.gov.cshr.job.repository.requiredLearningReminders.RequiredLearningReminderOrganisationUnitActionRepository;
import uk.gov.cshr.service.jobs.JobRunner;
import uk.gov.cshr.service.jobs.requiredLearningReminders.producer.RequiredLearningReminderProducerJob;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequiredLearningProducerJobTest extends IntegrationTestBase {

    @Autowired
    private RequiredLearningReminderProducerJob job;

    @Autowired
    private JobRunner jobRunner;

    @Autowired
    private RequiredLearningReminderOrganisationUnitActionRepository repository;

    @Test
    @Transactional
    public void testGenerateOrganisationActionRows() {
        stubService.getIdentityServiceStubService().getClientToken();
        stubService.getCslServiceStubService().getRequiredLearningForCourses("2023-01-02", "{}");
        stubService.getCslServiceStubService().getRequiredLearningForCourses("2023-01-15", "{}");
        stubService.getCslServiceStubService().getRequiredLearningForCourses("2023-01-31", """
                {
                    "course1": {
                        "title": "course 1",
                        "learningPeriods": {
                            "aud1": {
                                "learningPeriodStartTimestamp": "2022-01-01T00:00:00",
                                "learningPeriodEnd": "2023-01-31",
                                "organisationIds": [3, 4]
                            },
                            "aud2": {
                                "learningPeriodStartTimestamp": "2021-01-01T00:00:00",
                                "learningPeriodEnd": "2023-01-31",
                                "organisationIds": [5]
                            }
                        }
                    },
                    "course2": {
                        "title": "course 2",
                        "learningPeriods": {
                            "aud2": {
                                "learningPeriodStartTimestamp": "2022-01-01T00:00:00",
                                "learningPeriodEnd": "2023-01-31",
                                "organisationIds": [3, 4, 5]
                            }
                        }
                    }
                }
                """);
        jobRunner.run(job);
        List<RequiredLearningReminderOrganisationUnitAction> all = repository.findAll();
        assertEquals(6, all.size());

        Map<String, LocalDateTime> resultMap = all.stream()
                .collect(Collectors.toMap(row -> String.format("%s:%s", row.getCourseId(), row.getOrganisationId()),
                        RequiredLearningReminderOrganisationUnitAction::getLearningPeriodStart));

        LocalDateTime explp1Start = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
        LocalDateTime explp2Start = LocalDateTime.of(2021, 1, 1, 0, 0, 0);

        assertEquals(explp1Start, resultMap.get("course1:3"));
        assertEquals(explp1Start, resultMap.get("course1:4"));
        assertEquals(explp2Start, resultMap.get("course1:5"));
        assertEquals(explp1Start, resultMap.get("course2:3"));
        assertEquals(explp1Start, resultMap.get("course2:4"));
        assertEquals(explp1Start, resultMap.get("course2:5"));
    }
}
