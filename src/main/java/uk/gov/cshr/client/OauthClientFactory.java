package uk.gov.cshr.client;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OauthClientFactory {

    private final RestTemplateOAuthInterceptor restTemplateOAuthInterceptor;
    private final RestTemplateBuilder restTemplateBuilder;

    public OauthClientFactory(RestTemplateOAuthInterceptor restTemplateOAuthInterceptor, RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateOAuthInterceptor = restTemplateOAuthInterceptor;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    public IHttpClient buildClient(String baseUrl) {
        RestTemplate restTemplate = buildClientBuilder(baseUrl).build();
        return new HttpClient(restTemplate);
    }
    
    public RestTemplateBuilder buildClientBuilder(String baseUrl) {
        return restTemplateBuilder
                .baseUri(baseUrl)
                .additionalInterceptors(restTemplateOAuthInterceptor);
    }
}
