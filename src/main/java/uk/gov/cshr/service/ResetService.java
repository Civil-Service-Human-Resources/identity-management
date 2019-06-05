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
import uk.gov.cshr.repository.ResetRepository;

import java.util.Date;
import java.util.Set;

@Service
@Transactional
public class ResetService {

    private ResetRepository resetRepository;

    public ResetService(ResetRepository resetRepository) {
        this.resetRepository = resetRepository;
    }

    public void deleteResetsByIdentity(Identity identity) {
        resetRepository.deleteByEmail(identity.getEmail());
    }
}