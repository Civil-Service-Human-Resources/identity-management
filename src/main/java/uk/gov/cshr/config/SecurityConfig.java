package uk.gov.cshr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.cshr.service.security.IdentityService;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resource.jwt.key-value}")
    private String jwtKey;

    @Value("${identity.signoutUrl}")
    private String signoutUrl;

    @Value("${identity.idmLoginUrl}")
    private String idmLoginUrl;

    private final IdentityService identityService;

    public SecurityConfig(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService) {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/public/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/identity-service")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService)
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/mgmt/sign-out")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .addLogoutHandler((_, _, _) -> identityService.logoutUser())
                        .logoutSuccessUrl(signoutUrl + "?returnTo=" + idmLoginUrl)
                );

        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService(JwtDecoder clientJwtDecoder) {
        return userRequest -> {
            String tokenValue = userRequest.getAccessToken().getTokenValue();
            Map<String, Object> claims;
            Jwt jwt = clientJwtDecoder.decode(tokenValue);
            claims = jwt.getClaims();
            return new CustomOAuth2User(
                    ((ArrayList<String>) claims.get("authorities")).stream().map(SimpleGrantedAuthority::new).toList(),
                    claims,
                    "user_name", (String) claims.get("user_name"), (String) claims.get("email"), jwt
            );
        };
    }


    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = jwtKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}
