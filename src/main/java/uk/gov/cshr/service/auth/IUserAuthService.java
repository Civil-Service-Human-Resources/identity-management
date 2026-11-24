package uk.gov.cshr.service.auth;

import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.cshr.config.CustomOAuth2User;

public interface IUserAuthService {

    CustomOAuth2User getAuthentication();

    uk.gov.cshr.client.model.IdentityDto getIdentity();

    Jwt getBearerTokenFromUserAuth();
}
