package uk.gov.cshr.service.jobs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseJobConfig {

    @NotNull
    private String cron;

    @NotNull
    private Boolean enabled;

}
