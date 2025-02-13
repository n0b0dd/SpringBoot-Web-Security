package com.kosign.spring_security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;



@RestController("/api/v1/test")
@SecurityRequirement(name = "bearerAuth")
public class TestController {
    
    @GetMapping("/show")
    public String getMethodName() {
        return "Hello world";
    }

    @PostMapping("/concat")
    public String postMethodName(@RequestBody String entity) {
        //TODO: process POST request
        
        return "OK ! " + entity ;
    }
    
    
}
