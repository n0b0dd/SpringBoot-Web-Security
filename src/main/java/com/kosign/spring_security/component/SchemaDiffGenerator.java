package com.kosign.spring_security.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class SchemaDiffGenerator {
    
    public List<String> generateSchemaDiff(String oldSchema, String newSchema) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode oldNode = mapper.readTree(oldSchema);
            JsonNode newNode = mapper.readTree(newSchema);
            
            return compareSchemas(oldNode, newNode, "");
        } catch (JsonProcessingException e) {
            // log.error("Failed to generate schema diff", e);
            return Collections.emptyList();
        }
    }
    
    private List<String> compareSchemas(JsonNode oldNode, JsonNode newNode, String path) {
        List<String> differences = new ArrayList<>();
        
        // Compare fields
        Iterator<String> oldFields = oldNode.fieldNames();
        while (oldFields.hasNext()) {
            String fieldName = oldFields.next();
            String currentPath = path.isEmpty() ? fieldName : path + "." + fieldName;
            
            if (!newNode.has(fieldName)) {
                differences.add("Removed field: " + currentPath);
            } else {
                JsonNode oldValue = oldNode.get(fieldName);
                JsonNode newValue = newNode.get(fieldName);
                
                if (!oldValue.getNodeType().equals(newValue.getNodeType())) {
                    differences.add("Changed type: " + currentPath + 
                                  " from " + oldValue.getNodeType() + 
                                  " to " + newValue.getNodeType());
                } else if (oldValue.isObject()) {
                    differences.addAll(compareSchemas(oldValue, newValue, currentPath));
                }
            }
        }
        
        // Check for new fields
        Iterator<String> newFields = newNode.fieldNames();
        while (newFields.hasNext()) {
            String fieldName = newFields.next();
            if (!oldNode.has(fieldName)) {
                String currentPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                differences.add("Added field: " + currentPath);
            }
        }
        
        return differences;
    }
}