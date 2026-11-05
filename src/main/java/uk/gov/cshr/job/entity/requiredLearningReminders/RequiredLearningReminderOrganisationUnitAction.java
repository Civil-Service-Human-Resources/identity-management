package uk.gov.cshr.job.entity.requiredLearningReminders;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.cshr.job.entity.JobExecution;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "required_learning_reminder_organisation_unit_actions")
public class RequiredLearningReminderOrganisationUnitAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private JobExecution jobExecution;

    @Column(nullable = false)
    private Integer organisationId;

    @Column(nullable = false, length = 50)
    private String courseId;

    @Column(nullable = false)
    private String courseTitle;

    @Column(nullable = false)
    private LocalDateTime learningPeriodStart;

    @Column(nullable = false)
    private LocalDate learningPeriodEnd;

    @Column()
    private LocalDateTime processedTimestamp;

    public RequiredLearningReminderOrganisationUnitAction(JobExecution jobExecution, Integer organisationId, String courseId, String courseTitle, LocalDateTime learningPeriodStart, LocalDate learningPeriodEnd) {
        this.jobExecution = jobExecution;
        this.organisationId = organisationId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.learningPeriodStart = learningPeriodStart;
        this.learningPeriodEnd = learningPeriodEnd;
    }
}
