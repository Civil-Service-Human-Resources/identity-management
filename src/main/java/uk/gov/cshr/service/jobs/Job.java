package uk.gov.cshr.service.jobs;

import uk.gov.cshr.domain.jobs.JobType;
import uk.gov.cshr.job.entity.JobExecution;

public interface Job {

    JobType getJobType();

    String getCron();

    boolean isEnabled();

    void run(JobExecution jobExecution);

}
