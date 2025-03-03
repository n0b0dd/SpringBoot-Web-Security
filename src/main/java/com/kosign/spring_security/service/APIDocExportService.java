package com.kosign.spring_security.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kosign.spring_security.model.dto.APIChangeRecord;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class APIDocExportService {
    
    private final ChangelogGenerator changelogGenerator;
    private final GoogleSheetsService sheetsService;
    
    public void exportDocumentation() {
        // Generate Markdown changelog
        // String changelog = changelogGenerator.generateMarkdown();
        
        // Export to different formats
        // exportToMarkdown(changelog);
        // exportToPDF(changelog);
        
        // Update Google Sheets summary
        updateGoogleSheetsSummary();
    }
    
    private void updateGoogleSheetsSummary() {
        List<APIChangeRecord> allChanges = sheetsService.fetchAllChanges();
        
        // Create summary statistics
        Map<String, Long> changesByType = allChanges.stream()
            .collect(Collectors.groupingBy(APIChangeRecord::getChangeType, Collectors.counting()));
            
        long breakingChanges = allChanges.stream()
            .filter(APIChangeRecord::isBreakingChange)
            .count();
            
        // Update summary sheet
        sheetsService.updateSummarySheet(changesByType, breakingChanges);
    }
}