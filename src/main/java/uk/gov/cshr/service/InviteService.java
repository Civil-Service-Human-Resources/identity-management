package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.InviteRepository;

@Service
@Transactional
@Slf4j
public class InviteService {
    private final InviteRepository inviteRepository;

    public InviteService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    public Integer deleteInvitesByIdentity(Identity identity) {
        String email = identity.getEmail();
        Long inviterId = identity.getId();
        log.info("Deleting invites with email {} and inviter id {}", email, inviterId);
        Integer deletedByEmail = inviteRepository.deleteAllByForEmail(email);
        log.info("Deleted {} invites by email", deletedByEmail);
        Integer deletedByInviter = inviteRepository.deleteAllByInviter(identity);
        log.info("Deleted {} invites by inviter", deletedByInviter);
        return deletedByEmail + deletedByInviter;
    }
}
