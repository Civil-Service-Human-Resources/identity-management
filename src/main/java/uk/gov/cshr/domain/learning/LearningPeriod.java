package uk.gov.cshr.domain.learning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

import static uk.gov.cshr.utils.DateUtil.formatDateForFE;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearningPeriod implements Serializable {

    private LocalDate startDate;
    private LocalDate endDate;

    public String getDisplayPreviousDueDate() {
        return formatDateForFE(startDate);
    }

    public String getDisplayNextDueDate() {
        return formatDateForFE(endDate);
    }

}
