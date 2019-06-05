package uk.gov.cshr.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.ResetRepository;
import uk.gov.cshr.repository.TokenRepository;

@Service
@Transactional
public class TokenService {

    private TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void deleteTokensByIdentity(Identity identity) {
        tokenRepository.deleteByUserName(identity.getUid());
    }
}