package com.kosign.spring_security.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kosign.spring_security.model.dto.APIChangeRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChangelogGenerator {
    private final GoogleSheetsService sheetsService;
    
    // public String generateMarkdown() {
    //     List<APIChangeRecord> changes = sheetsService.fetchAllChanges();
        
    //     // Group changes by version
    //     Map<String, List<APIChangeRecord>> changesByVersion = changes.stream()
    //         .collect(Collectors.groupingBy(APIChangeRecord::getVersion));
            
    //     StringBuilder changelog = new StringBuilder();
    //     changelog.append("# API Changelog\n\n");
        
    //     changesByVersion.forEach((version, versionChanges) -> {
    //         changelog.append(generateVersionSection(version, versionChanges));
    //     });
        
    //     return changelog.toString();
    // }
    
    private String generateVersionSection(String version, List<APIChangeRecord> changes) {
        StringBuilder section = new StringBuilder();
        section.append("## Version ").append(version).append("\n\n");
        
        // Breaking Changes
        List<APIChangeRecord> breakingChanges = changes.stream()
            .filter(APIChangeRecord::isBreakingChange)
            .collect(Collectors.toList());
            
        if (!breakingChanges.isEmpty()) {
            section.append("### ⚠️ Breaking Changes\n\n");
            breakingChanges.forEach(change -> {
                section.append("- ").append(change.getDescription()).append("\n");
                if (StringUtils.hasText(change.getMigrationGuide())) {
                    section.append("  - Migration Guide: ").append(change.getMigrationGuide()).append("\n");
                }
            });
            section.append("\n");
        }
        
        // New Features
        changes.stream()
            .filter(c -> "ADDED".equals(c.getChangeType()))
            .forEach(change -> {
                section.append("### New Features\n\n");
                section.append("- ").append(change.getDescription()).append("\n");
                appendExamples(section, change);
            });
            
        // Deprecated Features
        changes.stream()
            .filter(c -> "DEPRECATED".equals(c.getChangeType()))
            .forEach(change -> {
                section.append("### Deprecated\n\n");
                section.append("- ").append(change.getDescription()).append("\n");
                if (change.getDeprecationDate() != null) {
                    section.append("  - Will be removed on: ")
                          .append(change.getDeprecationDate().format(DateTimeFormatter.ISO_DATE))
                          .append("\n");
                }
            });
            
        return section.toString();
    }
    
    private void appendExamples(StringBuilder section, APIChangeRecord change) {
        if (StringUtils.hasText(change.getRequestExample())) {
            section.append("  Request Example:\n```json\n")
                   .append(change.getRequestExample())
                   .append("\n```\n");
        }
        if (StringUtils.hasText(change.getResponseExample())) {
            section.append("  Response Example:\n```json\n")
                   .append(change.getResponseExample())
                   .append("\n```\n");
        }
    }
}