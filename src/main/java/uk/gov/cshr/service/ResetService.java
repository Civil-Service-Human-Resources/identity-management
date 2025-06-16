package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.ResetRepository;

@Service
@Transactional
@Slf4j
public class ResetService {

    private final ResetRepository resetRepository;

    public ResetService(ResetRepository resetRepository) {
        this.resetRepository = resetRepository;
    }

    public Integer deleteResetsByIdentity(Identity identity) {
        String email = identity.getEmail();
        log.info("Deleting password resets for email {}", email);
        return resetRepository.deleteByEmail(email);
    }
}
