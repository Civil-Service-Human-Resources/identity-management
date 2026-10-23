package uk.gov.cshr.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAccessTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
    private final String EMAIL_KEY = "email";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        log.debug("Getting user email address from JWT token claims");
        AbstractAuthenticationToken baseAuth = delegate.convert(jwt);
        String email = jwt.getClaimAsString(EMAIL_KEY);
        return new CustomOAuth2Authentication(jwt, baseAuth.getAuthorities(), email);
    }
}
