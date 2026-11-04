package uk.gov.cshr.service.jobs;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Collection;

@Slf4j
@Configuration
public class JobSchedulingConfig implements SchedulingConfigurer {

    private final Collection<Job> jobs;
    private final JobRunner jobRunner;

    public JobSchedulingConfig(Collection<Job> jobs, JobRunner jobRunner) {
        this.jobs = jobs;
        this.jobRunner = jobRunner;
    }

    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar taskRegistrar) {
        log.info("Registering jobs. Found {} jobs.", jobs.size());
        Collection<Job> enabledJobs = jobs.stream().filter(Job::isEnabled).toList();
        log.info("Found {} enabled jobs", enabledJobs.size());
        enabledJobs
                .forEach(job -> {
                    Runnable jobTask = () -> {
                        jobRunner.run(job);
                    };
                    log.info("Registering job {} on cron schedule {}", job.getJobType().getName(), job.getCron());
                    taskRegistrar.addCronTask(jobTask, job.getCron());
                });
    }
}
