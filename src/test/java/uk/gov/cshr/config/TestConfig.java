package uk.gov.cshr.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import uk.gov.cshr.utils.CustomOAuth2AuthenticationProvider;

@TestConfiguration
public class TestConfig {

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new CustomOAuth2AuthenticationProvider();
    }

}
