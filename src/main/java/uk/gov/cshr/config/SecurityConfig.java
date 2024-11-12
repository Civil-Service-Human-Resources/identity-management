package uk.gov.cshr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
public class SecurityConfig {

    @Value("${security.oauth2.resource.jwt.key-value}")
    private String jwtKey;

    @Bean
    public TokenStore getTokenStore() throws Exception {
        CustomAccessTokenConverter converter = new CustomAccessTokenConverter();
        converter.setSigningKey(jwtKey);
        converter.afterPropertiesSet();
        return new JwtTokenStore(converter);
    }
}
