package uk.gov.cshr.domain.learning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static uk.gov.cshr.utils.DateUtil.formatDatetimeForFE;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Record {

    protected LocalDateTime lastUpdated;
    protected LocalDateTime completionDate;
    protected State status;

    public String getDisplayLastUpdated() {
        return formatDatetimeForFE(lastUpdated);
    }

    public String getDisplayCompletionDate() {
        return formatDatetimeForFE(completionDate);
    }

    public boolean isCompleted() {
        return status.equals(State.COMPLETED);
    }

    public boolean isInProgress() {
        return status.equals(State.IN_PROGRESS);
    }

    public boolean isStarted() {
        return !status.equals(State.NULL);
    }

    public String getDisplayStatus() {
        if (!isStarted()) {
            return "Not started";
        } else if (isCompleted()) {
            return "Completed";
        } else {
            return "In progress";
        }
    }
}
