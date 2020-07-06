package uk.gov.cshr.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.CSRSService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.ResetService;
import uk.gov.cshr.service.TokenService;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@Transactional
public class IdentityService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);

    private final IdentityRepository identityRepository;

    private InviteService inviteService;

    private ResetService resetService;

    private TokenService tokenService;

    private final PasswordEncoder passwordEncoder;

    private final LearnerRecordService learnerRecordService;

    private final CSRSService csrsService;

    private final NotificationService notificationService;

    private final MessageService messageService;

    private final int deactivationMonths;

    private final int notificationMonths;

    private final int deletionMonths;

    public IdentityService(@Value("${accountPeriodsInMonths.deactivation}") int deactivation,
                           @Value("${accountPeriodsInMonths.notification}") int notification,
                           @Value("${accountPeriodsInMonths.deletion}") int deletion,
                           IdentityRepository identityRepository,
                           PasswordEncoder passwordEncoder,
                           LearnerRecordService learnerRecordService,
                           CSRSService csrsService,
                           NotificationService notificationService,
                           MessageService messageService,
                           ResetService resetService,
                           TokenService tokenService) {
        this.identityRepository = identityRepository;
        this.passwordEncoder = passwordEncoder;
        this.learnerRecordService = learnerRecordService;
        this.csrsService = csrsService;
        this.notificationService = notificationService;
        this.messageService = messageService;
        this.deactivationMonths = deactivation;
        this.notificationMonths = notification;
        this.deletionMonths = deletion;
        this.resetService = resetService;
        this.tokenService = tokenService;
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
                }
            }
        }
    }

    @Transactional
    public void trackUserActivity() {
        Iterable<Identity> identities = identityRepository.findAll();

        LocalDateTime deactivationDate = LocalDateTime.now().minusMonths(deactivationMonths);
        LocalDateTime deletionNotificationDate = LocalDateTime.now().minusMonths(notificationMonths);
        LocalDateTime deletionDate = LocalDateTime.now().minusMonths(deletionMonths);

        LOGGER.info("deactivation date {}, deleteNotifyDate {}, deleteDate {}", deactivationDate, deletionNotificationDate, deletionDate);

        identities.forEach(identity -> {
            LocalDateTime lastLoggedIn = LocalDateTime.ofInstant(identity.getLastLoggedIn(), ZoneOffset.UTC);

            if (lastLoggedIn.isBefore(deletionDate)) {
                LOGGER.info("deleting identity {} ", identity.getEmail());
                notificationService.send(messageService.createDeletedMessage(identity));

                deleteIdentity(identity.getUid());
            } else if (lastLoggedIn.isBefore(deletionNotificationDate) && !identity.isDeletionNotificationSent()) {
                LOGGER.info("sending notify {} ", identity.getEmail());
                notificationService.send(messageService.createDeletionMessage(identity));
                identity.setDeletionNotificationSent(true);
                identityRepository.save(identity);
            } else if (identity.isActive() && lastLoggedIn.isBefore(deactivationDate)) {
                LOGGER.info("deactivating identity {} ", identity.getEmail());
                notificationService.send(messageService.createSuspensionMessage(identity));
                identity.setActive(false);
                identity.setAgencyTokenUid(null);
                identityRepository.save(identity);
            }
        });
    }

    public void clearUserTokens(Identity identity) {
        tokenService.deleteTokensByIdentity(identity);
    }
}
