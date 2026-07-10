package com.gabrielgua.realworld.api.security;

import com.gabrielgua.realworld.domain.model.User;
import com.gabrielgua.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthUtils {

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String getCurrentUserEmail() {
        var auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return auth.getName();
    }

    public boolean isAuthenticated() {
        var auth = getAuthentication();
        boolean authenticated = auth != null && 
                               auth.isAuthenticated() && 
                               !"anonymousUser".equals(auth.getName());
        
        log.debug("Authentication check: {}, principal: {}", 
                 authenticated, auth != null ? auth.getName() : "null");
        
        return authenticated;
    }
}