package uk.gov.cshr.domain.learning;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class UserLearningCourse {
    private String resourceId;
    private String title;
    private String status;
    private String completionDate;

    public String getDisplayStatus() {
        if (StringUtils.isBlank(status)) {
            return "Not started";
        }
        return status;
    }
}
