package uk.gov.cshr.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BasicHttpClientConfig {

    private final BasicHttpClientFactory basicHttpClientFactory;

    @Value("${identity.url}")
    private String identityBaseUrl;

    @Value("${security.oauth2.client.client-id}")
    private String identityClientId;

    @Value("${security.oauth2.client.client-secret}")
    private String identityClientSecret;

    public BasicHttpClientConfig(BasicHttpClientFactory basicHttpClientFactory) {
        this.basicHttpClientFactory = basicHttpClientFactory;
    }

    @Bean(name = "identityHttpClient")
    IHttpClient identityClient() {
        return basicHttpClientFactory.buildClient(identityBaseUrl, identityClientId, identityClientSecret);
    }
}
