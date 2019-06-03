package uk.gov.cshr.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import uk.gov.cshr.exceptions.ForbiddenException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (auth == null) {
            return false;
        }

        ObjectMapper oMapper = new ObjectMapper();
        LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>> o = oMapper.convertValue(auth, LinkedHashMap.class);
        LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> userAuthentication = o.get("userAuthentication");
        ArrayList<String> roles = userAuthentication.get("details").get("roles");
        return roles.contains("IDENTITY_MANAGER");
    }
}