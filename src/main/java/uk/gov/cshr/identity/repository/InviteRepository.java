package uk.gov.cshr.identity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.identity.entity.Identity;
import uk.gov.cshr.identity.entity.Invite;

@Repository
public interface InviteRepository extends PagingAndSortingRepository<Invite, Long> {

    Page<Invite> findAllByForEmailContains(Pageable pageable, String email);

    Page<Invite> findAllByInviterNotNullAndInvitedAtNotNull(Pageable pageable);

    Integer deleteAllByForEmail(String forEmail);

    Integer deleteAllByInviter(Identity inviter);
}
