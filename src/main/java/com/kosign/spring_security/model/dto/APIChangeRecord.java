package com.kosign.spring_security.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class APIChangeRecord {
    // Basic info
    private String endpoint;
    private String method;
    private String version;
    private LocalDateTime timestamp;
    private String changeType;
    private String description;
    private String changedBy;
    private boolean isBreakingChange;
    
    // Schema changes
    private String oldRequestSchema;
    private String newRequestSchema;
    private String oldResponseSchema;
    private String newResponseSchema;
    private List<String> removedFields;
    private List<String> addedFields;
    
    // Examples
    private String requestExample;
    private String responseExample;
    
    // Migration info
    private String migrationGuide;
    private LocalDateTime deprecationDate;
    private String backwardCompatibilityDuration;
    
    // Additional metadata
    private Map<String, Object> metadata;
}