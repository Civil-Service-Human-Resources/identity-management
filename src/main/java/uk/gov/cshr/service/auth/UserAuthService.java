package uk.gov.cshr.service.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import uk.gov.cshr.client.model.IdentityDto;
import uk.gov.cshr.config.CustomOAuth2User;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserAuthService implements IUserAuthService {

    public CustomOAuth2User getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User customAuth) {
            return customAuth;
        }
        throw new ClientAuthenticationErrorException("No valid CustomOAuth2User found in security context");
    }

    @Override
    public Jwt getBearerTokenFromUserAuth() {
        return getAuthentication().getToken();
    }

    @Override
    public IdentityDto getIdentity() {
        CustomOAuth2User auth = getAuthentication();
        List<String> roles = new ArrayList<>(auth.getRoles());
        String uid = auth.getUid();
        return new IdentityDto(uid, roles);
    }
}
