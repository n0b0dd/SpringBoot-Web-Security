package com.kosign.spring_security.configuration.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenValidationResult {
    private boolean valid;
    private String message;
}

