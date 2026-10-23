package uk.gov.cshr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.cshr.service.security.IdentityService;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key:${security.oauth2.resource.jwt.key-value:}}")
    private String jwtKey;

    @Value("${identity.signoutUrl}")
    private String signoutUrl;

    @Value("${identity.idmLoginUrl}")
    private String idmLoginUrl;

    private final IdentityService identityService;

    private final CustomAccessTokenConverter customJwtAuthenticationConverter;

    public SecurityConfig(IdentityService identityService, CustomAccessTokenConverter customJwtAuthenticationConverter) {
        this.identityService = identityService;
        this.customJwtAuthenticationConverter = customJwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter))
                )
                .logout(logout -> logout
                        .logoutUrl("/sign-out")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .addLogoutHandler((request, response, authentication) -> {
                            identityService.logoutUser();
                        })
                        .logoutSuccessUrl(signoutUrl + "?returnTo=" + idmLoginUrl)
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = jwtKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}
