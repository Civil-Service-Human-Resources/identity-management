package uk.gov.cshr.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.job.entity.JobExecution;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Integer> {
}
