package uk.gov.cshr.job.repository.requiredLearningReminders;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.cshr.job.entity.requiredLearningReminders.RequiredLearningReminderOrganisationUnitAction;

import java.time.LocalDate;
import java.util.List;

public interface RequiredLearningReminderOrganisationUnitActionRepository extends JpaRepository<RequiredLearningReminderOrganisationUnitAction, Integer> {

    List<RequiredLearningReminderOrganisationUnitAction> getByLearningPeriodEndEquals(LocalDate learningPeriodEnd);
}
