package uk.gov.cshr.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.*;
import uk.gov.cshr.service.csrs.CSRSService;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;
import uk.gov.cshr.service.reportingService.ReportingService;

import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

@Service
@Slf4j
@Transactional
public class IdentityService implements UserDetailsService {

    private final IdentityRepository identityRepository;

    private InviteService inviteService;

    private final ResetService resetService;

    private final LearnerRecordService learnerRecordService;

    private final CSRSService csrsService;

    private final ReportingService reportingService;

    private final RequestEntityFactory requestEntityFactory;

    private final RestTemplate restTemplate;

    public IdentityService(IdentityRepository identityRepository,
                           LearnerRecordService learnerRecordService,
                           CSRSService csrsService,
                           ReportingService reportingService,
                           ResetService resetService,
                           RestTemplate restTemplate,
                           RequestEntityFactory requestEntityFactory) {
        this.identityRepository = identityRepository;
        this.learnerRecordService = learnerRecordService;
        this.csrsService = csrsService;
        this.reportingService = reportingService;
        this.resetService = resetService;
        this.requestEntityFactory = requestEntityFactory;
        this.restTemplate = restTemplate;
    }

    @Transactional(propagation = REQUIRED, isolation = SERIALIZABLE, rollbackFor = Exception.class)
    public void deleteIdentity(String uid) {
        log.info("Deleting from learner-record");
        learnerRecordService.deleteCivilServant(uid);
        log.info("Deleting from civil-servant-registry");
        csrsService.deleteCivilServant(uid);
        log.info("Removing details from reporting");
        reportingService.removeUserDetails(uid);
        Optional<Identity> result = identityRepository.findFirstByUid(uid);
        if (result.isPresent()) {
            log.info("Deleting from identity");
            Identity identity = result.get();
            inviteService.deleteInvitesByIdentity(identity);
            resetService.deleteResetsByIdentity(identity);
            identityRepository.delete(identity);
            identityRepository.flush();
        }
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
        identity.setLocked(!identity.isLocked());
        identityRepository.save(identity);
    }

    public void logoutUser() {
        restTemplate.exchange(requestEntityFactory.createLogoutRequest(), Void.class);
    }
}
