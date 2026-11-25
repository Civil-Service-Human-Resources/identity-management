package uk.gov.cshr.identity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.identity.entity.Identity;
import uk.gov.cshr.identity.entity.Invite;

@Repository
public interface InviteRepository extends PagingAndSortingRepository<Invite, Long>, JpaRepository<Invite, Long> {

    Page<Invite> findAllByForEmailContains(Pageable pageable, String email);

    Page<Invite> findAllByInviterNotNullAndInvitedAtNotNull(Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Invite i where i.forEmail = :forEmail")
    long deleteByForEmail(String forEmail);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Invite i where i.inviter = :identity")
    long deleteByInviter(Identity identity);
}
