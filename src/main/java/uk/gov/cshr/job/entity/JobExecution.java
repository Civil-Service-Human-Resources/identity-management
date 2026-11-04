package uk.gov.cshr.job.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_executions")
public class JobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private JobStatus jobStatus;

    @Column(nullable = false)
    private LocalDateTime startTimestamp;

    @Column
    private LocalDateTime endTimestamp;

    public JobExecution(Job job, JobStatus jobStatus, LocalDateTime startTimestamp) {
        this.job = job;
        this.jobStatus = jobStatus;
        this.startTimestamp = startTimestamp;
    }
}
