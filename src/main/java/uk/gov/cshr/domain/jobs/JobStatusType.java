package uk.gov.cshr.domain.jobs;

import lombok.Getter;

@Getter
public enum JobStatusType {
    PENDING("PENDING"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");

    private final String type;

    JobStatusType(String type) {
        this.type = type;
    }
}
