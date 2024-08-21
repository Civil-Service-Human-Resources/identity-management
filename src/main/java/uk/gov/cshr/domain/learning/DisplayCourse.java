package uk.gov.cshr.domain.learning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisplayCourse extends Record {

    private String courseId;
    private String courseTitle;
    private String shortDescription;
    private DisplayAudience audience;
    private List<DisplayModule> modules;
    private Integer requiredModules;
    private Integer completedRequiredModules;

    public String getCourseTitleAsId() throws Exception {
        return URLEncoder.encode(courseTitle, StandardCharsets.UTF_8.toString());
    }



}
