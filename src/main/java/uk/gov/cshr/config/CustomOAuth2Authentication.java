package uk.gov.cshr.config;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomOAuth2Authentication extends OAuth2Authentication {

    private final String userEmail;

    /**
     * Construct an OAuth 2 authentication. Since some grant types don't require user authentication, the user
     * authentication may be null.
     *
     * @param storedRequest      The authorization request (must not be null).
     * @param userAuthentication The user authentication (possibly null).
     */
    public CustomOAuth2Authentication(OAuth2Request storedRequest, Authentication userAuthentication, String userEmail) {
        super(storedRequest, userAuthentication);
        this.userEmail = userEmail;
    }

    public Set<String> getRoles() {
        return this.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    public String getUid() {
        return (String) this.getPrincipal();
    }

}
