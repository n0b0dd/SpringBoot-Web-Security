package com.kosign.spring_security.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.kosign.spring_security.model.Version;
import com.kosign.spring_security.model.dto.APIChangeRecord;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "API vesion tracking";
    private final Sheets sheetsService;
    private final String spreadsheetId;

    @Value("${google.sheets.spreadsheet-id}")
    private String SPREADSHEET_ID;

    public GoogleSheetsService(
        @Value("${google.sheets.credentials-file}") String credentialsPath,
        @Value("${google.sheets.spreadsheet-id}") String spreadsheetId) throws IOException {
        // Initialize the Google Sheets service
        GoogleCredentials credentials = GoogleCredentials.fromStream(
            new ClassPathResource(credentialsPath).getInputStream())
            .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        this.sheetsService = new Sheets.Builder(
            new NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials))
            .setApplicationName(APPLICATION_NAME)
            .build();
            
        this.spreadsheetId = SPREADSHEET_ID;
    }
    /**
     * Logs an API change to the main API_Changes sheet
     */
    public void logAPIChange(APIChangeRecord change) {
        try {
            ValueRange body = new ValueRange()
                .setValues(Arrays.asList(
                    Arrays.asList(
                        LocalDateTime.now().toString(),
                        change.getEndpoint(),
                        change.getMethod(),
                        change.getVersion(),
                        change.getChangeType(),
                        change.getDescription(),
                        change.getChangedBy(),
                        change.isBreakingChange() ? "Yes" : "No",
                        change.getRequestExample(),
                        change.getResponseExample()
                    )
                ));

            // Append to API_Changes sheet
            Sheets.Spreadsheets.Values.Append request = sheetsService.spreadsheets().values()
                .append(spreadsheetId, "API_Changes!A:J", body)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS");

            AppendValuesResponse response = request.execute();
            log.info("Added API change to row: {}", response.getUpdates().getUpdatedRange());

            // If it's a breaking change, log to Breaking_Changes sheet
            if (change.isBreakingChange()) {
                logBreakingChange(change);
            }
        } catch (IOException e) {
            log.error("Failed to log API change", e);
            throw new RuntimeException("Failed to log API change", e);
        }
    }

    private void logBreakingChange(APIChangeRecord change) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

     /**
     * Updates the API summary dashboard
     */
    public void updateSummarySheet(Map<String, Long> changesByType, long breakingChanges) {
        try {
            List<List<Object>> values = new ArrayList<>();
            values.add(Arrays.asList("Total Changes", getTotalChanges()));
            values.add(Arrays.asList("Breaking Changes", breakingChanges));
            
            changesByType.forEach((type, count) -> 
                values.add(Arrays.asList(type + " Changes", count)));

            ValueRange body = new ValueRange().setValues(values);

            sheetsService.spreadsheets().values()
                .update(spreadsheetId, "API_Summary!A2:B", body)
                .setValueInputOption("RAW")
                .execute();
        } catch (IOException e) {
            log.error("Failed to update summary sheet", e);
        }
    }

    /**
     * Retrieves all API changes for a specific version
     */
    public List<APIChangeRecord> getChangesByVersion(String version) {
        try {
            ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, "API_Changes!A:J")
                .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                return Collections.emptyList();
            }

            return values.stream()
                .skip(1) // Skip header row
                .filter(row -> row.get(3).equals(version)) // Version is in column D (index 3)
                .map(this::mapToAPIChangeRecord)
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to fetch changes for version {}", version, e);
            return Collections.emptyList();
        }
    }

    /**
     * Maps a row from Google Sheets to an APIChangeRecord
     */
    private APIChangeRecord mapToAPIChangeRecord(List<Object> row) {
        return APIChangeRecord.builder()
            .timestamp(LocalDateTime.parse(row.get(0).toString()))
            .endpoint(row.get(1).toString())
            .method(row.get(2).toString())
            .version(row.get(3).toString())
            .changeType(row.get(4).toString())
            .description(row.get(5).toString())
            .changedBy(row.get(6).toString())
            .isBreakingChange("Yes".equals(row.get(7).toString()))
            .requestExample(row.size() > 8 ? row.get(8).toString() : null)
            .responseExample(row.size() > 9 ? row.get(9).toString() : null)
            .build();
    }

    /**
     * Generates a changelog for specified versions
     */
    public String generateChangelog(String fromVersion, String toVersion) {
        try {
            List<APIChangeRecord> changes = getChangesBetweenVersions(fromVersion, toVersion);
            StringBuilder changelog = new StringBuilder();
            changelog.append("# API Changes: ").append(fromVersion)
                    .append(" to ").append(toVersion).append("\n\n");

            // Group changes by type
            Map<String, List<APIChangeRecord>> changesByType = changes.stream()
                .collect(Collectors.groupingBy(APIChangeRecord::getChangeType));

            // Breaking changes first
            appendChangeSection(changelog, "Breaking Changes", 
                changes.stream().filter(APIChangeRecord::isBreakingChange)
                    .collect(Collectors.toList()));

            // Other changes by type
            changesByType.forEach((type, typeChanges) -> 
                appendChangeSection(changelog, type, typeChanges));

            return changelog.toString();
        } catch (Exception e) {
            log.error("Failed to generate changelog", e);
            return "Failed to generate changelog: " + e.getMessage();
        }
    }

    private void appendChangeSection(StringBuilder changelog, 
                                   String sectionTitle, 
                                   List<APIChangeRecord> changes) {
        if (!changes.isEmpty()) {
            changelog.append("## ").append(sectionTitle).append("\n\n");
            changes.forEach(change -> 
                changelog.append("- ").append(change.getDescription())
                        .append(" (").append(change.getEndpoint()).append(")\n"));
            changelog.append("\n");
        }
    }

    /**
     * Gets total number of changes
     */
    private long getTotalChanges() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
            .get(spreadsheetId, "API_Changes!A:A")
            .execute();
        
        return response.getValues() != null ? response.getValues().size() - 1 : 0;
    }

    public List<APIChangeRecord> fetchAllChanges() {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Gets changes between two versions
     * Example: getChangesBetweenVersions("v1.0", "v1.2")
     */
    public List<APIChangeRecord> getChangesBetweenVersions(String fromVersion, String toVersion) {
        try {
            // 1. Parse version strings
            Version from = parseVersion(fromVersion);
            Version to = parseVersion(toVersion);

            // 2. Fetch changes from Google Sheets
            ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, "API_Changes!A:J")
                .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                return Collections.emptyList();
            }

            // 3. Filter and map the changes
            return values.stream()
                .skip(1) // Skip header row
                .map(this::mapToAPIChangeRecord)
                .filter(change -> isVersionInRange(change.getVersion(), from, to))
                .sorted(compareByVersion())
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to fetch changes between versions {} and {}", 
                     fromVersion, toVersion, e);
            return Collections.emptyList();
        }
    }

    /**
     * Parses version string into Version object
     * Handles formats like: v1.0, 1.0.0, v1.0.0-beta
     */
    private Version parseVersion(String versionString) {
        // Remove 'v' prefix if present
        String cleaned = versionString.startsWith("v") ? 
            versionString.substring(1) : versionString;

        String[] parts = cleaned.split("\\.");
        String[] mainVersion = parts[0].split("-");

        return Version.builder()
            .major(Integer.parseInt(mainVersion[0]))
            .minor(parts.length > 1 ? Integer.parseInt(parts[1]) : 0)
            .patch(parts.length > 2 ? Integer.parseInt(parts[2]) : 0)
            .suffix(mainVersion.length > 1 ? mainVersion[1] : "")
            .build();
    }

    /**
     * Checks if a version is between two other versions
     */
    private boolean isVersionInRange(String versionStr, Version from, Version to) {
        Version version = parseVersion(versionStr);
        return (compareVersions(version, from) >= 0 && 
                compareVersions(version, to) <= 0);
    }

    /**
     * Compares two versions
     * Returns: -1 if v1 < v2, 0 if v1 = v2, 1 if v1 > v2
     */
    private int compareVersions(Version v1, Version v2) {
        if (v1.getMajor() != v2.getMajor()) {
            return Integer.compare(v1.getMajor(), v2.getMajor());
        }
        if (v1.getMinor() != v2.getMinor()) {
            return Integer.compare(v1.getMinor(), v2.getMinor());
        }
        if (v1.getPatch() != v2.getPatch()) {
            return Integer.compare(v1.getPatch(), v2.getPatch());
        }
        // Handle suffixes (e.g., -alpha, -beta)
        return v1.getSuffix().compareTo(v2.getSuffix());
    }

    /**
     * Creates a comparator for sorting changes by version
     */
    private Comparator<APIChangeRecord> compareByVersion() {
        return (c1, c2) -> {
            Version v1 = parseVersion(c1.getVersion());
            Version v2 = parseVersion(c2.getVersion());
            return compareVersions(v1, v2);
        };
    }

}