package uk.gov.cshr.service.jobs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BaseJobConfig {

    @NotNull
    private final String cron;

    @NotNull
    private final boolean enabled;

}
