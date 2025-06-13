package uk.gov.cshr.domain;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class DeleteUserResults {

    private Integer deletedRegisteredLearners;
    private Integer updatedCourseCompletions;

    @Override
    public String toString() {
        return "deletedRegisteredLearners: " + deletedRegisteredLearners + ", updatedCourseCompletions: " + updatedCourseCompletions;
    }

}
