package uk.gov.cshr.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("frontendAuthService")
public class FrontendAuthService {

    public boolean hasPermission(Permission permission) {
        return this.authHasPermission(SecurityContextHolder.getContext().getAuthentication(), permission);
    }

    public boolean authHasPermission(Authentication authentication, Permission permission) {
        return authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(permission.getMappedRole()));
    }
}
