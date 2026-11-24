package uk.gov.cshr.identity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.identity.entity.Identity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IdentityRepository extends JpaRepository<Identity, Long> {

    default List<Identity> findForDeactivation(LocalDateTime cutoff) {
        return this.fetchForDRJob(cutoff, true, false);
    }

    default List<Identity> findForDeletion(LocalDateTime cutoff) {
        return this.fetchForDRJob(cutoff, false, true);
    }

    default List<Identity> findForDeletionNotification(LocalDateTime cutoff) {
        return this.fetchForDRJob(cutoff, false, false);
    }

    @Query("select i " +
            "from Identity i " +
            "where i.lastLoggedIn < :cutoff " +
            "and not exists (" +
            "select 1 " +
            "from Reactivation r " +
            "where r.email = i.email " +
            "and r.reactivatedAt >= :cutoff" +
            ") " +
            "and i.active = :active " +
            "and i.deletionNotificationSent = :deletionNotificationSent")
    List<Identity> fetchForDRJob(@Param("cutoff") LocalDateTime cutoff, @Param("active") boolean active,
                                 @Param("deletionNotificationSent") boolean deletionNotificationSent);

    Page<Identity> findAllByEmailContains(Pageable pageable, String email);

    Optional<Identity> findFirstByUid(String uid);

}
