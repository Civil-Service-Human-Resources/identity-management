package uk.gov.cshr.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Request;
import uk.gov.cshr.config.CustomOAuth2Authentication;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthUtils {

    public static CustomOAuth2Authentication getOAuth2User(Set<String> roles) {
        roles.add("ROLE_USER");
        OAuth2Request oAuth2Request = mock(OAuth2Request.class);
        Authentication authentication = mock(CustomOAuth2Authentication.class);
        when(authentication.getPrincipal()).thenReturn("user");
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection grantedAuthorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        when(authentication.getAuthorities()).thenReturn(grantedAuthorities);
        CustomOAuth2Authentication auth = new CustomOAuth2Authentication(oAuth2Request, authentication, "email@email.com");
        auth.setAuthenticated(true);
        return auth;
    }

}
