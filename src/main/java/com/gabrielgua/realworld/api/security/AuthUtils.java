package com.gabrielgua.realworld.api.security;

import com.gabrielgua.realworld.domain.model.User;
import com.gabrielgua.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String getCurrentUserEmail() {
        Authentication auth = getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        return auth.getName();
    }

    public boolean isAuthenticated() {
        Authentication auth = getAuthentication();
        return auth != null && auth.isAuthenticated();
    }
}
