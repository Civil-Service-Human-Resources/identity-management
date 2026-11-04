package uk.gov.cshr.client.cslService.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@AllArgsConstructor
public class LearningPeriodWithOrgs {

    private LocalDateTime learningPeriodStartTimestamp;
    private LocalDateTime learningPeriodEndTimestamp;
    private Collection<Integer> organisationIds;
}
