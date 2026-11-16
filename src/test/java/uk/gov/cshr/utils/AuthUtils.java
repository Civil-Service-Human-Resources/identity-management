package uk.gov.cshr.utils;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.cshr.config.CustomOAuth2User;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthUtils {

    public static CustomOAuth2User getOAuth2User(Set<String> roles) {
        roles.add("ROLE_USER");

        Collection<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Jwt jwt = Jwt.withTokenValue("mock-token-value")
                .header("alg", "none")
                .claim("email", "email@email.com")
                .claim("user_name", "userId")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        return new CustomOAuth2User(authorities, jwt.getClaims(), "user_name", "userId", "email@email.com", jwt);
    }
}
