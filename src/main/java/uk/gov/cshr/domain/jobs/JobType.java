package uk.gov.cshr.domain.jobs;

import lombok.Getter;

@Getter
public enum JobType {
    REQUIRED_LEARNING_NOTIFICATION_PRODUCER("REQUIRED_LEARNING_NOTIFICATION_PRODUCER");

    private final String name;

    JobType(String name) {
        this.name = name;
    }
}
