package uk.gov.cshr.utils.stub;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.stereotype.Service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Service
public class IdentityServiceStubService {

    public void getClientToken() {
        stubFor(
                WireMock.post(urlPathEqualTo("/identity/oauth2/token"))
                        .withQueryParam("grant_type", equalTo("client_credentials"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                        {
                                            "access_token": "token",
                                            "token_type": "Bearer",
                                            "expires_in": 43199
                                        }"""))
        );
    }

}
