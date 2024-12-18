package uk.gov.cshr.domain.learning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisplayAudience {

    private String name;
    private String frequency;
    private LearningPeriod learningPeriod;

}
