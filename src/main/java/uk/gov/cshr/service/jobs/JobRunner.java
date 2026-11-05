package uk.gov.cshr.service.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.job.entity.JobExecution;

@Slf4j
@Service
public class JobRunner {

    private final JobService jobService;

    public JobRunner(JobService jobService) {
        this.jobService = jobService;
    }

    public void run(Job job) {
        JobExecution jobExecution = jobService.createJob(job.getJobType());
        try {
            job.run(jobExecution);
            jobService.saveJob(jobExecution);
        } catch (Exception e) {
            log.error("Required learning notification job failed: {}", String.valueOf(e));
            jobService.failJob(jobExecution);
            throw e;
        }
    }

}
