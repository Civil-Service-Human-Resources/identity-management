package uk.gov.cshr.service.jobs;

import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.jobs.JobType;
import uk.gov.cshr.job.entity.Job;
import uk.gov.cshr.job.entity.JobExecution;
import uk.gov.cshr.job.entity.JobStatus;
import uk.gov.cshr.job.repository.JobExecutionRepository;
import uk.gov.cshr.job.repository.JobRepository;
import uk.gov.cshr.job.repository.JobStatusRepository;
import uk.gov.cshr.utils.IUtilService;

import static uk.gov.cshr.domain.jobs.JobStatusType.*;

@Service
public class JobService {

    private final IUtilService utilService;
    private final JobRepository jobRepository;
    private final JobStatusRepository jobStatusRepository;
    private final JobExecutionRepository jobExecutionRepository;

    public JobService(IUtilService utilService, JobRepository jobRepository, JobStatusRepository jobStatusRepository, JobExecutionRepository jobExecutionRepository) {
        this.utilService = utilService;
        this.jobRepository = jobRepository;
        this.jobStatusRepository = jobStatusRepository;
        this.jobExecutionRepository = jobExecutionRepository;
    }

    public JobExecution createJob(JobType jobType) {
        Job job = jobRepository.findByJobType(jobType);
        JobStatus jobStatus = jobStatusRepository.findByJobStatusType(IN_PROGRESS);
        JobExecution jobExecution = new JobExecution(job, jobStatus, utilService.getNowDateTime());
        jobExecutionRepository.save(jobExecution);
        return jobExecution;
    }

    public void failJob(JobExecution jobExecution) {
        jobExecution.setJobStatus(jobStatusRepository.findByJobStatusType(FAILED));
        jobExecutionRepository.save(jobExecution);
    }

    public void saveJob(JobExecution job) {
        job.setEndTimestamp(utilService.getNowDateTime());
        job.setJobStatus(jobStatusRepository.findByJobStatusType(COMPLETED));
        jobExecutionRepository.save(job);
    }
}
