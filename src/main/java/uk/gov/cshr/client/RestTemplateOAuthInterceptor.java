package uk.gov.cshr.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import uk.gov.cshr.service.auth.IBearerTokenService;

import java.io.IOException;

@Component
public class RestTemplateOAuthInterceptor implements ClientHttpRequestInterceptor {

    private final IBearerTokenService bearerTokenService;

    public RestTemplateOAuthInterceptor(IBearerTokenService bearerTokenService) {
        this.bearerTokenService = bearerTokenService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!request.getHeaders().containsHeader("Authorization")) {
            String token = bearerTokenService.getBearerToken();
            request.getHeaders().setBearerAuth(token);
        }
        return execution.execute(request, body);
    }
}
