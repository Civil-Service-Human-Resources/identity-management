package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Reactivation;

import java.util.List;

@Repository
public interface ReactivationRepository extends CrudRepository<Reactivation, Long> {

    List<Reactivation> findByEmail(String email);

}
