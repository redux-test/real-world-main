package com.gabrielgua.realworld.api.security;

import com.gabrielgua.realworld.api.assembler.UserAssembler;
import com.gabrielgua.realworld.api.model.user.UserAuthenticate;
import com.gabrielgua.realworld.api.model.user.UserRegister;
import com.gabrielgua.realworld.api.model.user.UserResponse;
import com.gabrielgua.realworld.domain.service.ProfileService;
import com.gabrielgua.realworld.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final UserAssembler userAssembler;
    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegister register) {
        log.info("User registration attempt for email: {}", register.getEmail());
        
        var user = userAssembler.toEntity(register);
        var profile = profileService.createNewProfile(user, register.getUsername());
        
        var response = authService.register(userService.save(user, profile));
        
        log.info("User registration successful for email: {}", register.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> authenticate(@Valid @RequestBody UserAuthenticate authenticate) {
        log.info("User login attempt for email: {}", authenticate.getEmail());
        
        var response = authService.authenticate(authenticate);
        
        // Ensure token is properly formatted and not null
        if (response.getToken() == null || response.getToken().isEmpty()) {
            log.error("Authentication failed - no token generated for email: {}", authenticate.getEmail());
            throw new RuntimeException("Authentication failed - token generation error");
        }
        
        log.info("User login successful for email: {}", authenticate.getEmail());
        return ResponseEntity.ok(response);
    }
}