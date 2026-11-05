package uk.gov.cshr.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.cshr.domain.jobs.JobType;
import uk.gov.cshr.job.entity.Job;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Integer> {
    Optional<Job> findByJobName(String jobName);

    default Job findByJobType(JobType jobType) {
        return findByJobName(jobType.getName())
                .orElseThrow(() -> new RuntimeException("Job " + jobType.getName() + " not found"));
    }
}
