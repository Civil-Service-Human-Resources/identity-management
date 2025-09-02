package uk.gov.cshr.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteUserResults {

    private Integer deletedRegisteredLearners;
    private Integer updatedCourseCompletions;

    @Override
    public String toString() {
        return "deletedRegisteredLearners: " + deletedRegisteredLearners + ", updatedCourseCompletions: " + updatedCourseCompletions;
    }

}
