package com.kosign.spring_security.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kosign.spring_security.model.Role;
import com.kosign.spring_security.model.User;
import com.kosign.spring_security.model.dto.AuthDto;
import com.kosign.spring_security.model.dto.AuthDto.AuthenticationRequest;
import com.kosign.spring_security.model.dto.AuthDto.LoginResponse;
import com.kosign.spring_security.model.dto.AuthDto.AuthResponse;
import com.kosign.spring_security.model.dto.AuthDto.RegisterRequest;
import com.kosign.spring_security.model.dto.UserDto.UserResponse;
import com.kosign.spring_security.repository.UserRepository;
import com.kosign.spring_security.utils.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
        
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    
    @Transactional
    public AuthDto.LoginResponse register(RegisterRequest request) {
        // Validate if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email already registered", HttpStatus.BAD_REQUEST);
        }

        // Create new user
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(true)
                // .createdAt(LocalDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        return buildAuthResponse(savedUser, token, refreshToken);
    }

    public AuthDto.LoginResponse authenticate(AuthenticationRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Get user and update last login
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
            
            userRepository.save(user);
            
            // Generate JWT token
            String token = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            return buildAuthResponse(user, token, refreshToken);
            
        } catch (Exception e) {
            throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @Transactional()
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
    }

     public void logout() {
        SecurityContextHolder.clearContext();
    }

    public boolean validateToken(String token) {
        try {
            String userEmail = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
            
            return jwtService.isTokenValid(token, user);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        User currentUser = getCurrentUser();
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new CustomException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }
        
        // Update password
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);
    }

    private LoginResponse buildAuthResponse(User user, String token, String refreshToken) {
        return LoginResponse.builder()
                .auth(AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .refreshToken(refreshToken)
                    .build())
                .user(UserResponse.builder()
                        .id(user.getId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .build();
    }


     public AuthResponse refreshToken(String refreshToken) {
        // Validate refresh token
        String userEmail = jwtService.extractUsername(refreshToken);
        UserDetails user = userDetailsService.loadUserByUsername(userEmail);
        
        if (jwtService.isTokenValid(refreshToken, user)) {
            String newAccessToken = jwtService.generateToken(user);
            return AuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .build();
        }
        throw new CustomException("Refresh token is invalid", HttpStatus.UNAUTHORIZED);
    }
}