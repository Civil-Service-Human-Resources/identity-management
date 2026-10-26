package uk.gov.cshr.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import uk.gov.cshr.client.OAuthTokenService;
import uk.gov.cshr.client.model.OAuthToken;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static io.micrometer.common.util.StringUtils.isBlank;

@Service
@Slf4j
public class BearerTokenService implements IBearerTokenService {

    private final Clock clock;
    private final OAuthTokenService oAuthTokenService;
    private final IUserAuthService userAuthService;
    @Value("${spring.security.oauth2.refresh.service-token-cache.before-seconds-to-expire}")
    private long refreshServiceTokenCacheBeforeSecondsToExpire;

    public BearerTokenService(Clock clock, OAuthTokenService oAuthTokenService, IUserAuthService userAuthService) {
        this.clock = clock;
        this.oAuthTokenService = oAuthTokenService;
        this.userAuthService = userAuthService;
    }

    public String getBearerToken() {
        String bearerToken = "";
        Jwt jwtPrincipal = userAuthService.getBearerTokenFromUserAuth();
        if (jwtPrincipal != null) {
            bearerToken = jwtPrincipal.getTokenValue();
        }
        if (isBlank(bearerToken)) {
            OAuthToken serviceToken = oAuthTokenService.getCachedOAuthServiceToken();
            LocalDateTime tokenExpiry = serviceToken.getExpiryDateTime();
            log.debug("serviceToken: expiryDateTime: {}", tokenExpiry);
            long secondsRemainingToExpire = tokenExpiry != null ?
                    ChronoUnit.SECONDS.between(LocalDateTime.now(clock), tokenExpiry) : 0;
            log.debug("serviceToken: seconds remaining to service token expiry: {}", secondsRemainingToExpire);
            log.debug("serviceToken: seconds remaining to refresh the service token cache: {}",
                    (secondsRemainingToExpire - refreshServiceTokenCacheBeforeSecondsToExpire));
            if (secondsRemainingToExpire <= refreshServiceTokenCacheBeforeSecondsToExpire) {
                oAuthTokenService.removeServiceTokenFromCache();
                serviceToken = oAuthTokenService.getCachedOAuthServiceToken();
            }
            bearerToken = serviceToken.getAccessToken();
        }
        return bearerToken;
    }
}
