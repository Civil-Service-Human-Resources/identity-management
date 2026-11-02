package uk.gov.cshr.client.identity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import uk.gov.cshr.client.IHttpClient;

@Component
@Slf4j
public class IdentityAPIClient implements IIdentityAPIClient {

    private final IHttpClient client;
    @Value("${identity.oauthLogoutEndpoint}")
    private String oauthLogoutEndpoint;

    public IdentityAPIClient(@Qualifier("identityOAuthHttpClient") IHttpClient client) {
        this.client = client;
    }

    @Override
    public void logoutUser() {
        client.executeRequest(RequestEntity.get(oauthLogoutEndpoint).build(), Void.class);
    }
}
