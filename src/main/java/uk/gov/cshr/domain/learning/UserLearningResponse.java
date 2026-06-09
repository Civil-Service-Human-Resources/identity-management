package uk.gov.cshr.domain.learning;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLearningResponse {
    private List<UserLearningCourse> learning;
    private int page;
    private int size;
    private int totalResults;
}
