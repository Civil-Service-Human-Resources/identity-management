package uk.gov.cshr.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.*;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class IdentityService implements UserDetailsService {

    private final IdentityRepository identityRepository;

    private InviteService inviteService;

    private ResetService resetService;

    private TokenService tokenService;

    private final LearnerRecordService learnerRecordService;

    private final CSRSService csrsService;

    private final NotificationService notificationService;

    private final MessageService messageService;

    private final int deactivationMonths;

    private final int notificationMonths;

    private final int deletionMonths;

    private final RequestEntityFactory requestEntityFactory;

    private final RestTemplate restTemplate;

    public IdentityService(@Value("${accountPeriodsInMonths.deactivation}") int deactivation,
                           @Value("${accountPeriodsInMonths.notification}") int notification,
                           @Value("${accountPeriodsInMonths.deletion}") int deletion,
                           IdentityRepository identityRepository,
                           LearnerRecordService learnerRecordService,
                           CSRSService csrsService,
                           NotificationService notificationService,
                           MessageService messageService,
                           ResetService resetService,
                           TokenService tokenService,
                           RestTemplate restTemplate,
                           RequestEntityFactory requestEntityFactory) {
        this.identityRepository = identityRepository;
        this.learnerRecordService = learnerRecordService;
        this.csrsService = csrsService;
        this.notificationService = notificationService;
        this.messageService = messageService;
        this.deactivationMonths = deactivation;
        this.notificationMonths = notification;
        this.deletionMonths = deletion;
        this.resetService = resetService;
        this.tokenService = tokenService;
        this.requestEntityFactory = requestEntityFactory;
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setInviteService(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(username);
        if (identity == null) {
            throw new UsernameNotFoundException("No user found with email address " + username);
        }
        return new IdentityDetails(identity);
    }

    @ReadOnlyProperty
    public boolean existsByEmail(String email) {
        return identityRepository.existsByEmail(email);
    }

    public Identity getIdentity(String uid) {
        return identityRepository.findFirstByUid(uid)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public void updateLocked(String uid) {
        Identity identity = identityRepository.findFirstByUid(uid)
                .orElseThrow(ResourceNotFoundException::new);

        if (identity.isLocked()) {
            identity.setLocked(false);
        } else {
            identity.setLocked(true);
        }
        identityRepository.save(identity);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public void deleteIdentity(String uid) {
        ResponseEntity lrResponse = learnerRecordService.deleteCivilServant(uid);

        if (lrResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
            ResponseEntity csrsResponse = csrsService.deleteCivilServant(uid);

            if (csrsResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
                Optional<Identity> result = identityRepository.findFirstByUid(uid);

                if (result.isPresent()) {
                    Identity identity = result.get();
                    inviteService.deleteInvitesByIdentity(identity);
                    resetService.deleteResetsByIdentity(identity);
                    tokenService.deleteTokensByIdentity(identity);
                    identityRepository.delete(identity);
                    identityRepository.flush();
                }
            }
        }
    }

    @Transactional
    public void trackUserActivity() {
        log.info("Starting trackUserActivity");

        LocalDateTime deactivationDate = LocalDateTime.now().minusMonths(deactivationMonths);
        LocalDateTime deletionNotificationDate = LocalDateTime.now().minusMonths(notificationMonths);
        LocalDateTime deletionDate = LocalDateTime.now().minusMonths(deletionMonths);

        log.info("deactivation date {}, deleteNotifyDate {}, deleteDate {}", deactivationDate, deletionNotificationDate, deletionDate);

        Iterable<Identity> identities = identityRepository.findByLastLoggedInBefore(deactivationDate.toInstant(ZoneOffset.UTC));

        identities.forEach(identity -> {
            LocalDateTime lastLoggedIn = LocalDateTime.ofInstant(identity.getLastLoggedIn(), ZoneOffset.UTC);

            if (lastLoggedIn.isBefore(deletionDate)) {
                log.info("deleting identity {} ", identity.getEmail());
                notificationService.send(messageService.createDeletedMessage(identity));

                deleteIdentity(identity.getUid());
            } else if (lastLoggedIn.isBefore(deletionNotificationDate) && !identity.isDeletionNotificationSent()) {
                log.info("sending notify {} ", identity.getEmail());
                notificationService.send(messageService.createDeletionMessage(identity));
                identity.setDeletionNotificationSent(true);
                identityRepository.saveAndFlush(identity);
            } else if (identity.isActive() && lastLoggedIn.isBefore(deactivationDate)) {
                log.info("deactivating identity {} ", identity.getEmail());
                notificationService.send(messageService.createSuspensionMessage(identity));
                identity.setActive(false);
                identity.setAgencyTokenUid(null);
                identityRepository.saveAndFlush(identity);
            }
        });

        if (restTemplate.exchange(requestEntityFactory.createLogoutRequest(), Void.class).getStatusCode().is2xxSuccessful()) {
            log.info("Management client user logged out after data retention execution");
        } else {
            log.error("Error logging out management client user after data retention execution, this may cause future executions to be unstable");
        }

      log.info("Finished trackUserActivity");

    }

    public void clearUserTokens(Identity identity) {
        tokenService.deleteTokensByIdentity(identity);
    }
}
