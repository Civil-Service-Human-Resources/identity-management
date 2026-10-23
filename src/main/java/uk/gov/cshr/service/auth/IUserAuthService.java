package uk.gov.cshr.service.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public interface IUserAuthService {

    Authentication getAuthentication();

    String getUsername();

    uk.gov.cshr.client.model.IdentityDto getIdentity();

    Jwt getBearerTokenFromUserAuth();
}
