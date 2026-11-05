package uk.gov.cshr.client.cslService.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@AllArgsConstructor
public class LearningPeriodWithOrgs {

    private LocalDateTime learningPeriodStartTimestamp;
    private LocalDate learningPeriodEnd;
    private Collection<Integer> organisationIds;
}
