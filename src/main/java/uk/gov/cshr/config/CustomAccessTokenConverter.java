package uk.gov.cshr.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class CustomAccessTokenConverter extends JwtAccessTokenConverter {

    private final String EMAIL_KEY = "email";

    @Override
    public Map<String, ?> convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        log.debug("Getting user email address from access token");
        Map<String, Object> vals = (Map<String, Object>) super.convertAccessToken(token, authentication);
        vals.put(EMAIL_KEY, token.getAdditionalInformation().get(EMAIL_KEY));
        return vals;
    }

    @Override
    public CustomOAuth2Authentication extractAuthentication(Map<String, ?> map) {
        OAuth2Authentication authentication = super.extractAuthentication(map);
        String email = (String) map.get(EMAIL_KEY);
        return new CustomOAuth2Authentication(authentication.getOAuth2Request(), authentication.getUserAuthentication(), email);
    }

}
