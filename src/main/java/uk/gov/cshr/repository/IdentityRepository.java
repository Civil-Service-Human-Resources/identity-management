package uk.gov.cshr.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Identity;

import java.util.Optional;

@Repository
public interface IdentityRepository extends PagingAndSortingRepository<Identity, Long> {

    Identity findFirstByActiveTrueAndEmailEquals(String email);

    boolean existsByEmail(String email);

    Optional<Identity> findFirstByUid(String uid);

}
