package uk.gov.cshr.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uk.gov.cshr.client.model.IdentityDto;
import uk.gov.cshr.config.CustomOAuth2User;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class UserAuthService implements IUserAuthService {

    public CustomOAuth2User getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User customAuth) {
            return customAuth;
        }
        throw new ClientAuthenticationErrorException("No valid CustomOAuth2User found in security context");
    }

    @Override
    public String getBearerTokenFromUserAuth() {
        try {
            return getAuthentication().getToken().getTokenValue();
        } catch (ClientAuthenticationErrorException e) {
            log.error("CustomOAuth2user does not exist in security context. Returning blank token");
            return "";
        }
    }

    @Override
    public IdentityDto getIdentity() {
        CustomOAuth2User auth = getAuthentication();
        List<String> roles = new ArrayList<>(auth.getRoles());
        String uid = auth.getUid();
        return new IdentityDto(uid, roles);
    }
}
