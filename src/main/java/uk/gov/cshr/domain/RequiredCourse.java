package uk.gov.cshr.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static uk.gov.cshr.utils.DateUtil.formatDatetimeForFE;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequiredCourse {

    private String courseId;
    private String courseTitle;
    private String shortDescription;
    private Instant lastUpdated;
    private Instant completionDate;
    private Instant dueDate;
    private String status;
    private Audience audience;
    private List<Module> modules;

    public String getDisplayLastUpdated() {
        return formatDatetimeForFE(lastUpdated);
    }

    public String getDisplayCompletionDate() {
        return formatDatetimeForFE(completionDate);
    }

    public String getDisplayDueDate() {
        return formatDatetimeForFE(dueDate);
    }

    public Integer[] getRequiredModuleCount() {
        int requiredModules = 0;
        int completedRequiredModules = 0;
        for (Module module : this.modules) {
            if (!module.isOptional()) {
                requiredModules++;
                if (module.getStatus().equals("COMPLETED")) {
                    completedRequiredModules++;
                }
            }

        }
        return new Integer[]{completedRequiredModules, requiredModules};
    }

    public String getCourseTitleAsId() throws Exception {
        return URLEncoder.encode(courseTitle, StandardCharsets.UTF_8.name());
    }

}
