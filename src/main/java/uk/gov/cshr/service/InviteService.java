package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.InviteRepository;

@Service
@Transactional
public class InviteService {
    private final InviteRepository inviteRepository;

    @Autowired
    public InviteService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    public void deleteInvitesByIdentity(Identity identity) {
        inviteRepository.deleteByForEmail(identity.getEmail());
        inviteRepository.deleteByInviterId(identity.getId());
    }
}
