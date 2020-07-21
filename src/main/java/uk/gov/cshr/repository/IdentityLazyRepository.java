package uk.gov.cshr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.cshr.domain.IdentityLazy;

public interface IdentityLazyRepository extends JpaRepository<IdentityLazy, Long> {
}
