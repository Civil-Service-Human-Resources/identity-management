package uk.gov.cshr.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static uk.gov.cshr.utils.DateUtil.formatDatetimeForFE;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Module {

    private String id;
    private String moduleTitle;
    private String shortDescription;
    private boolean optional;
    private Instant lastUpdated;
    private Instant completionDate;
    private String status;

    public String getDisplayLastUpdated() {
        return formatDatetimeForFE(lastUpdated);
    }

    public String getDisplayCompletionDate() {
        return formatDatetimeForFE(completionDate);
    }

    public boolean isCompleted() {
        return status.equals("COMPLETED");
    }

    public boolean isInProgress() {
        return status.equals("IN_PROGRESS");
    }

    public boolean isStarted() {
        return this.isCompleted() || this.isInProgress();
    }

    public String getDisplayStatus() {
        switch (status) {
            case "COMPLETED": return "Completed";
            case "IN_PROGRESS": return "In progress";
            default: return "Not started";
        }
    }

}
