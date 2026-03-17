package uk.gov.cshr.domain.learning;

import lombok.Data;

@Data
public class UserLearningCourse {
    private String resourceId;
    private String title;
    private String status;
    private String completionDate;
}
