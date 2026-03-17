package uk.gov.cshr.domain.learning;

import lombok.Data;
import java.util.List;

@Data
public class UserLearningResponse {
    private List<UserLearningCourse> learning;
    private int page;
    private int size;
    private long totalResults;
}
