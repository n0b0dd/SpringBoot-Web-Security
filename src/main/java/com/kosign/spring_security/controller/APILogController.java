package com.kosign.spring_security.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kosign.spring_security.model.dto.APIChangeRecord;
import com.kosign.spring_security.service.GoogleSheetsService;
import com.kosign.spring_security.utils.annotations.APIVersion;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/log")
@APIVersion("v1")
@Tag(name = "API log", description = "API log tracking")
public class APILogController {

    private GoogleSheetsService sheetsService;

    @Autowired  // You can also omit @Autowired if you only have one constructor
    public APILogController(GoogleSheetsService sheetsService) {
        if(sheetsService == null){
            System.out.println("sheetsService is null during initialization");
        }
            this.sheetsService = sheetsService;
    }

    @PostMapping("/changes")
    public ResponseEntity<String> logAPIChange(@RequestBody APIChangeRecord change) {
        sheetsService.logAPIChange(change);
        return ResponseEntity.ok("API change logged successfully");
    }

    @GetMapping("/changes")
    public ResponseEntity<List<APIChangeRecord>> getChanges(
        @RequestParam(required = false) String version
    ) {
        List<APIChangeRecord> changes = version != null ?
        sheetsService.getChangesByVersion(version) : 
        sheetsService.fetchAllChanges();
        return ResponseEntity.ok(changes);
    }
    @GetMapping("/changelog")
    public ResponseEntity<String> getChangelog(
        @RequestParam String fromVersion,
        @RequestParam String toVersion
    ) {
        String changelog = sheetsService.generateChangelog(fromVersion, toVersion);
        return ResponseEntity.ok(changelog);
    }
}
