package uk.gov.cshr.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomOAuth2Authentication extends JwtAuthenticationToken {

    private final String userEmail;

    /**
     * Construct a modern custom JWT authentication token.
     *
     * @param jwt          The decoded JWT token (must not be null).
     * @param authorities  The collection of granted authorities/roles.
     * @param userEmail    The extracted user email claim.
     */
    public CustomOAuth2Authentication(Jwt jwt, Collection<? extends GrantedAuthority> authorities, String userEmail) {
        super(jwt, authorities);
        this.userEmail = userEmail;
    }

    public Set<String> getRoles() {
        return this.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public String getUid() {
        // In modern JWTs, the unique user identifier is standardly stored in the 'sub' (subject) claim.
        // If your legacy app used a different claim (like 'user_id' or 'uid'), change this to:
        // return this.getToken().getClaimAsString("uid");
        return this.getToken().getSubject();
    }
}
