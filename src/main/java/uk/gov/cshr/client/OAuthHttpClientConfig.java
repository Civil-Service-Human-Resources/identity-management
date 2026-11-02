package uk.gov.cshr.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuthHttpClientConfig {

    @Value("${learnerRecord.serviceUrl}")
    private String learnerRecordBaseUrl;
    @Value("${reporting.serviceUrl}")
    private String reportServiceBaseUrl;
    @Value("${notifications.service}")
    private String notificationServiceBaseUrl;
    @Value("${identity.url}")
    private String identityBaseUrl;
    @Value("${csrs.serviceUrl}")
    private String csrsBaseUrl;
    @Value("${cslService.serviceUrl}")
    private String cslServiceBaseUrl;

    private final OauthClientFactory oauthClientFactory;

    public OAuthHttpClientConfig(OauthClientFactory oauthClientFactory) {
        this.oauthClientFactory = oauthClientFactory;
    }

    @Bean(name = "learnerRecordHttpClient")
    public IHttpClient learnerRecordClient() {
        return oauthClientFactory.buildClient(learnerRecordBaseUrl);
    }

    @Bean(name = "reportServiceHttpClient")
    public IHttpClient reportServiceClient() {
        return oauthClientFactory.buildClient(reportServiceBaseUrl);
    }

    @Bean(name = "notificationServiceHttpClient")
    IHttpClient notificationServiceClient() {
        return oauthClientFactory.buildClient(notificationServiceBaseUrl);
    }

    @Bean(name = "identityOAuthHttpClient")
    IHttpClient identityOAuthClient() {
        return oauthClientFactory.buildClient(identityBaseUrl);
    }

    @Bean(name = "csrsHttpClient")
    IHttpClient csrsClient() {
        return oauthClientFactory.buildClient(csrsBaseUrl);
    }

    @Bean(name = "cslServiceHttpClient")
    IHttpClient cslServiceClient() {
        return oauthClientFactory.buildClient(cslServiceBaseUrl);
    }
}
