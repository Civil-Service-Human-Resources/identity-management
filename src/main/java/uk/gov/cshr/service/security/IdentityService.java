package uk.gov.cshr.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.client.identity.IIdentityAPIClient;
import uk.gov.cshr.identity.entity.Identity;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.identity.repository.IdentityRepository;

@Service
@Slf4j
@Transactional
public class IdentityService {

    private final IdentityRepository identityRepository;
    private final IIdentityAPIClient identityAPIClient;

    public IdentityService(IdentityRepository identityRepository, IIdentityAPIClient identityAPIClient) {
        this.identityRepository = identityRepository;
        this.identityAPIClient = identityAPIClient;
    }

    public Identity getIdentity(String uid) {
        return identityRepository.findFirstByUid(uid)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public Identity updateLockStatus(String uid) {
        Identity identity = identityRepository.findFirstByUid(uid)
                .orElseThrow(ResourceNotFoundException::new);
        identity.setLocked(!identity.isLocked());
        return identityRepository.save(identity);
    }

    public void logoutUser() {
        identityAPIClient.logoutUser();
    }
}
