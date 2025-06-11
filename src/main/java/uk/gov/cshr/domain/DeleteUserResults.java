package uk.gov.cshr.domain;


import lombok.Data;

@Data
public class DeleteUserResults {

    private final Integer deletedRegisteredLearners;
    private final Integer updatedCourseCompletions;

    @Override
    public String toString() {
        return "deletedRegisteredLearners: " + deletedRegisteredLearners + ", updatedCourseCompletions: " + updatedCourseCompletions;
    }

}
