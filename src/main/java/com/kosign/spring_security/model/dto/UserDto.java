package com.kosign.spring_security.model.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRequest {
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        private String firstname;

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        private String lastname;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
        private String phoneNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String firstname;
        private String lastname;
        private String email;
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserUpdateRequest {
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        private String firstname;

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        private String lastname;

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
        private String phoneNumber;
    }
}