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
