package uk.gov.cshr.service.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import uk.gov.cshr.client.model.IdentityDto;
import uk.gov.cshr.config.CustomOAuth2Authentication;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserAuthService implements IUserAuthService {

    public CustomOAuth2Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof CustomOAuth2Authentication customAuth) {
            return customAuth;
        }
        throw new ClientAuthenticationErrorException("No valid CustomOAuth2Authentication found in security context");
    }

    @Override
    public Jwt getBearerTokenFromUserAuth() {
        return getAuthentication().getToken();
    }

    @Override
    public String getUsername() {
        CustomOAuth2Authentication auth = getAuthentication();
        String username = auth.getToken().getClaimAsString("user_name");
        if (StringUtils.isBlank(username)) {
            throw new ClientAuthenticationErrorException("Learner Id is missing from authentication token");
        }
        return username;
    }

    @Override
    public IdentityDto getIdentity() {
        CustomOAuth2Authentication auth = getAuthentication();
        List<String> roles = new ArrayList<>(auth.getRoles());
        String uid = auth.getToken().getClaimAsString("user_name");

        if (StringUtils.isBlank(uid)) {
            uid = auth.getUid();
        }

        return new IdentityDto(uid, roles);
    }
}
