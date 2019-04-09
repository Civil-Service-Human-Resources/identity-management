package uk.gov.cshr.service;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.InviteRepository;

import java.util.Date;
import java.util.Set;

@Service
@Transactional
public class InviteService {
    @Autowired
    private NotificationService notificationService;

    private final MessageService messageService;

    private InviteRepository inviteRepository;

    @Value("${invite.validityInSeconds}")
    private int validityInSeconds;

    @Autowired
    public InviteService(InviteRepository inviteRepository, MessageService messageService) {
        this.inviteRepository = inviteRepository;
        this.messageService = messageService;
    }

    @ReadOnlyProperty
    public Invite findByCode(String code) {
        return inviteRepository.findByCode(code);
    }

    public boolean isCodeExpired(String code) {
        Invite invite = inviteRepository.findByCode(code);
        long diffInMs = new Date().getTime() - invite.getInvitedAt().getTime();

        if (diffInMs > validityInSeconds * 1000 && invite.getStatus().equals(InviteStatus.PENDING)) {
            updateInviteByCode(code, InviteStatus.ACCEPTED);
            return true;
        }

        updateInviteByCode(code, InviteStatus.EXPIRED);
        return false;
    }

    public void updateInviteByCode(String code, InviteStatus newStatus) {
        Invite invite = inviteRepository.findByCode(code);
        invite.setStatus(newStatus);
        inviteRepository.save(invite);
    }

    public void createNewInviteForEmailAndRoles(String email, Set<Role> roleSet, Identity inviter) {
        Invite invite = new Invite();
        invite.setForEmail(email);
        invite.setForRoles(roleSet);
        invite.setInviter(inviter);
        invite.setInvitedAt(new Date());
        invite.setStatus(InviteStatus.PENDING);
        invite.setCode(RandomStringUtils.random(40, true, true));

        notificationService.send(messageService.createInvitationnMessage(invite));

        inviteRepository.save(invite);
    }

    public void deleteInvitesByIdentity(Identity identity) {
        inviteRepository.deleteByForEmail(identity.getEmail());
        inviteRepository.deleteByInviterId(identity.getId());
    }
}