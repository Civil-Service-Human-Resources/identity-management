package uk.gov.cshr.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static uk.gov.cshr.utils.DateUtil.formatDateForFE;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Audience {

    private String organisation;
    private String frequency;
    private Instant previousDueDate;
    private Instant nextDueDate;

    public String getDisplayPreviousDueDate() {
        return formatDateForFE(previousDueDate);
    }

    public String getDisplayNextDueDate() {
        return formatDateForFE(nextDueDate);
    }

}
