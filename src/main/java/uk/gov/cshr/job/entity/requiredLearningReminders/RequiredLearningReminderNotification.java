package uk.gov.cshr.job.entity.requiredLearningReminders;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.cshr.job.entity.JobNotification;

import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "required_learning_reminder_notifications")
public class RequiredLearningReminderNotification implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private JobNotification jobNotificationId;

    @Column(length = 50)
    private String courseId;
}
