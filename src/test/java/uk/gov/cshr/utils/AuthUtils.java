package uk.gov.cshr.utils;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.cshr.config.CustomOAuth2Authentication;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthUtils {

    public static CustomOAuth2Authentication getOAuth2User(Set<String> roles) {
        roles.add("ROLE_USER");

        Collection<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Jwt jwt = Jwt.withTokenValue("mock-token-value")
                .header("alg", "none")
                .claim("email", "email@email.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        CustomOAuth2Authentication auth = new CustomOAuth2Authentication(jwt, authorities, "email@email.com");
        auth.setAuthenticated(true);
        return auth;
    }
}
