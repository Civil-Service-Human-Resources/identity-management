package uk.gov.cshr.identity.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.identity.entity.Reactivation;
import uk.gov.cshr.identity.entity.ReactivationStatus;

import java.util.Date;
import java.util.List;

@Repository
public interface ReactivationRepository extends CrudRepository<Reactivation, Long> {

    List<Reactivation> findByEmailAndReactivationStatusEquals(String email, ReactivationStatus status);

    List<Reactivation> findByReactivatedAtAfter(Date reactivatedAt);
}
