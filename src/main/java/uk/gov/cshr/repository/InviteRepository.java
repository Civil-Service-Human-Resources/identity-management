package uk.gov.cshr.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Invite;

@Repository
public interface InviteRepository extends PagingAndSortingRepository<Invite, Long> {

    Page<Invite> findAllByForEmailContains(Pageable pageable, String email);

    Page<Invite> findAllByInviterNotNullAndInvitedAtNotNull(Pageable pageable);

    void deleteByForEmail(String forEmail);

    void deleteByInviterId(Long inviterId);
}
