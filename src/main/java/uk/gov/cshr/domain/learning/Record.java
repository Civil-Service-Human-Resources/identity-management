package uk.gov.cshr.domain.learning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static uk.gov.cshr.utils.DateUtil.formatDatetimeForFE;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Record {

//    @JsonFormat(timezone = "UTC", shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSSS")
    protected LocalDateTime lastUpdated;
//    @JsonFormat(timezone = "UTC", shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSSS")
    protected LocalDateTime completionDate;
    protected State status;

    public String getDisplayLastUpdated() {
        return lastUpdated == null ? "Never" : formatDatetimeForFE(lastUpdated.toInstant(ZoneOffset.UTC));
    }

    public String getDisplayCompletionDate() {
        return completionDate == null ? "Never" : formatDatetimeForFE(completionDate.toInstant(ZoneOffset.UTC));
    }

    public boolean isCompleted() {
        return status.equals(State.COMPLETED);
    }

    public boolean isInProgress() {
        return status.equals(State.IN_PROGRESS);
    }

    public boolean isNull() {
        return status.equals(State.NULL);
    }

    public String getDisplayStatus() {
        if (isNull()) {
            if (lastUpdated == null) {
                return "Not started";
            }
            return "No progress in current learning period";
        } else if (isCompleted()) {
            return "Completed";
        } else {
            return "In progress";
        }
    }
}
