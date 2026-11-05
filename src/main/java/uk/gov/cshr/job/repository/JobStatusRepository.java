package uk.gov.cshr.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.cshr.domain.jobs.JobStatusType;
import uk.gov.cshr.job.entity.JobStatus;

import java.util.Optional;

public interface JobStatusRepository extends JpaRepository<JobStatus, Integer> {

    Optional<JobStatus> findByJobStatus(String jobStatus);

    default JobStatus findByJobStatusType(JobStatusType jobStatusType) {
        return findByJobStatus(jobStatusType.getType())
                .orElseThrow(() -> new RuntimeException("Job status " + jobStatusType.getType() + " not found"));
    }

}
