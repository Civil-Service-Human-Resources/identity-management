package uk.gov.cshr.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import static java.lang.String.format;

@Component
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        return this.hasPermission(auth, (Permission) permission);
    }

    private boolean hasPermission(Authentication auth, Permission permission) {
        if (auth != null) {
            log.debug(format("Evaluating user %s against permission %s (Role: %s)", auth.getPrincipal(), permission, permission.getMappedRole()));
            return auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(permission.getMappedRole()));
        }
        return false;
    }
}
