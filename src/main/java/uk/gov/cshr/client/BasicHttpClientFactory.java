package uk.gov.cshr.client;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BasicHttpClientFactory {

    private final RestTemplateBuilder restTemplateBuilder;

    public BasicHttpClientFactory(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    public IHttpClient buildClient(String baseUrl, String username, String password) {
        RestTemplate restTemplate = buildClientBuilder(baseUrl, username, password).build();
        return new HttpClient(restTemplate);
    }

    public RestTemplateBuilder buildClientBuilder(String baseUrl, String username, String password) {
        return restTemplateBuilder
                .baseUri(baseUrl)
                .basicAuthentication(username, password);
    }
}
