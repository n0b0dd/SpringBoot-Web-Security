package com.kosign.spring_security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kosign.spring_security.model.dto.AuthDto.AuthResponse;
import com.kosign.spring_security.model.dto.AuthDto.AuthenticationRequest;
import com.kosign.spring_security.model.dto.AuthDto.LoginResponse;
import com.kosign.spring_security.model.dto.AuthDto.RegisterRequest;
import com.kosign.spring_security.service.AuthService;
import com.kosign.spring_security.utils.annotations.APIVersion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@APIVersion("v1")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @APIVersion(
        value = "v1.1",
        since = "2024-02-14",
        description = "Added email validation",
        breaking = true,
        migrationGuide = "Update client to handle new email validation requirements",
        backwardCompatibilityDuration = "30d",
        requestExample = RegisterRequest.class,
        responseExample = LoginResponse.class
    )
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user")
    public ResponseEntity<LoginResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current user")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change user password")
    public ResponseEntity<Void> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        authService.changePassword(oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<Boolean> validateToken(
            @RequestParam String token
    ) {
        return ResponseEntity.ok(authService.validateToken(token));
    }

    @GetMapping("/refresh-token")
    @Operation(summary = "Get new token")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestParam String refreshToken
    ) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}

