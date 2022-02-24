package uk.gov.cshr.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Identity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface IdentityRepository extends JpaRepository<Identity, Long> {

    Identity findFirstByActiveTrueAndEmailEquals(String email);

    List<Identity> findByLastLoggedInBefore(Instant date);

    List<Identity> findByActiveTrueAndLastLoggedInBefore(Instant deactivationDate);

    List<Identity> findByDeletionNotificationSentFalseAndLastLoggedInBefore(Instant deletionReminderDate);

    Page<Identity> findAllByEmailContains(Pageable pageable, String email);

    boolean existsByEmail(String email);

    Optional<Identity> findFirstByUid(String uid);

}
