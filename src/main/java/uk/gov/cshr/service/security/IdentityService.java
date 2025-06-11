package uk.gov.cshr.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.RequestEntityFactory;

@Service
@Slf4j
@Transactional
public class IdentityService {

    private final IdentityRepository identityRepository;

    private final RequestEntityFactory requestEntityFactory;

    private final RestTemplate restTemplate;

    public IdentityService(IdentityRepository identityRepository,
                           RestTemplate restTemplate,
                           RequestEntityFactory requestEntityFactory) {
        this.identityRepository = identityRepository;
        this.requestEntityFactory = requestEntityFactory;
        this.restTemplate = restTemplate;
    }

//    @Transactional(propagation = REQUIRED, isolation = SERIALIZABLE, rollbackFor = Exception.class)
//    public void deleteIdentity(Identity identity, boolean adminDeletion) {
//        String uid = identity.getUid();
//        log.info("Deleting from learner-record");
//        learnerRecordService.deleteCivilServant(uid);
//        log.info("Deleting from civil-servant-registry");
//        csrsService.deleteCivilServant(uid);
//        reportingService.removeUserDetails(uid, adminDeletion);
//        inviteService.deleteInvitesByIdentity(identity);
//        resetService.deleteResetsByIdentity(identity);
//        identityRepository.delete(identity);
//    }
//
//    @Transactional(propagation = REQUIRED, isolation = SERIALIZABLE, rollbackFor = Exception.class)
//    public void deactivateIdentities(List<Identity> identities, boolean adminDeactivation) {
//        identities.forEach(u -> u.setActive(false));
//        identityRepository.save(identities);
//        reportingService.deactivateRegisteredLearners(identities.stream().map(Identity::getUid).collect(Collectors.toList()), adminDeactivation);
//    }

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
