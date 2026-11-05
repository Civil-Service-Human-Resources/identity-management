package uk.gov.cshr.client.cslService.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CourseWithLearningPeriods {

    private String title;
    private Map<String, LearningPeriodWithOrgs> learningPeriods;

}
