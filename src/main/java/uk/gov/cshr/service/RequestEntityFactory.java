package uk.gov.cshr.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class RequestEntityFactory {
    public RequestEntity createDeleteRequest(URI uri) {
        return new RequestEntity(getOauth2HeadersFromSecurityContext(), HttpMethod.DELETE, uri);
    }


    public RequestEntity createDeleteRequest(String uri) {
        try {
            return createDeleteRequest(new URI(uri));
        } catch (URISyntaxException e) {
            throw new RequestEntityException(e);
        }
    }

    public RequestEntity createPostRequest(URI uri, Object body) {
        getOauth2HeadersFromSecurityContext();
        return new RequestEntity(body, getOauth2HeadersFromSecurityContext(), HttpMethod.POST, uri);
    }

    public RequestEntity createPostRequest(String uri, Object body) {
        try {
            return createPostRequest(new URI(uri), body);
        } catch (URISyntaxException e) {
            throw new RequestEntityException(e);
        }
    }

    private HttpHeaders getOauth2HeadersFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + details.getTokenValue());
        return headers;
    }
}
