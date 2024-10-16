package uk.gov.cshr.service;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

@Component
public class RequestEntityFactory {

    @Value("${identity.url}")
    private String identityUrl;

    @Value("${security.oauth2.client.client-id}")
    private String clientId;

    @Value("${security.oauth2.client.client-secret}")
    private String clientSecret;

    @Value("${security.oauth2.client.access-token-uri}")
    private String clientUrl;

    @Value("${identity.oauthLogoutEndpoint}")
    private String oauthLogoutEndpoint;

    private <T> RequestEntity <T> createDeleteRequest(URI uri) {
        return new RequestEntity<>(getOauth2HeadersFromSecurityContext(), HttpMethod.DELETE, uri);
    }

    public <T> RequestEntity <T> createDeleteRequest(String uri) {
        try {
            return createDeleteRequest(new URI(uri));
        } catch (URISyntaxException e) {
            throw new RequestEntityException(e);
        }
    }

    private <T> RequestEntity <T> createGetRequest(URI uri) {
        return new RequestEntity<>(getOauth2HeadersFromSecurityContext(), HttpMethod.GET, uri);
    }

    public <T> RequestEntity <T> createGetRequest(String uri) {
        try {
            return createGetRequest(new URI(uri));
        } catch (URISyntaxException e) {
            throw new RequestEntityException(e);
        }
    }

    private <T> RequestEntity <T> createPutRequest(URI uri, T body) {
        return new RequestEntity<>(body, getOauth2HeadersFromSecurityContext(), HttpMethod.PUT, uri);
    }

    public <T> RequestEntity <T> createPutRequest(String uri, T body) {
        try {
            return createPutRequest(new URI(uri), body);
        } catch (URISyntaxException e) {
            throw new RequestEntityException(e);
        }
    }

    private <T> RequestEntity <T> createPostRequest(URI uri, T body) {
        return new RequestEntity<>(body, getOauth2HeadersFromSecurityContext(), HttpMethod.POST, uri);
    }

    public <T> RequestEntity <T> createGetRequest(URI uri, T body) {
        return new RequestEntity<>(body, getOauth2HeadersFromSecurityContext(), HttpMethod.GET, uri);
    }

    public <T> RequestEntity <T> createPostRequest(String uri, T body) {
        try {
            return createPostRequest(new URI(uri), body);
        } catch (URISyntaxException e) {
            throw new RequestEntityException(e);
        }
    }

    public <T> RequestEntity <T> createLogoutRequest() {
        try {
            return createGetRequest(new URI(identityUrl + oauthLogoutEndpoint), null);
        } catch (URISyntaxException e) {
            throw new RequestEntityException(e);
        }
    }


    private HttpHeaders getOauth2HeadersFromSecurityContext() {
        String token;
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication != null) {
            OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
            token = details.getTokenValue();
        } else {
            token = getNewAccessToken();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return headers;
    }

    private String getNewAccessToken() {
        RestTemplate restTemplate = new RestTemplate();

        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes()));

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Basic " + encodedCredentials);

        HttpEntity<String> request = new HttpEntity<String>(headers);

        String access_token_url = clientUrl;
        access_token_url += "?grant_type=client_credentials";

        ResponseEntity<String> response = restTemplate.exchange(access_token_url, HttpMethod.POST, request, String.class);

        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getString("access_token");
    }
}
