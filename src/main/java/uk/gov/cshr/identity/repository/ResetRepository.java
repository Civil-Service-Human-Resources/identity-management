package uk.gov.cshr.identity.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.identity.entity.Reset;

@Repository
public interface ResetRepository extends CrudRepository<Reset, Long> {
    Integer deleteByEmail(String email);
}
