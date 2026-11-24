package uk.gov.cshr.config;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private final String uid;
    private final String email;
    private final Jwt token;

    public CustomOAuth2User(@Nullable Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes, String nameAttributeKey, String uid, String email, Jwt token) {
        super(authorities, attributes, nameAttributeKey);
        this.uid = uid;
        this.email = email;
        this.token = token;
    }
    
    public Set<String> getRoles() {
        return this.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

}

