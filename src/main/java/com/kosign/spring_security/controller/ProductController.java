package com.kosign.spring_security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kosign.spring_security.model.Product;

@RestController("/api/v1/products")
@Tag(name = "Product", description = "Product management APIs")
@SecurityRequirement(name = "bearerAuth") // Add this for controller-level security
public class ProductController {

    @Operation(
        summary = "Create a new product",
        description = "Creates a new product with the provided details"
    )
    @SecurityRequirement(name = "bearerAuth") // Or add for method-level security
    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(product);
    }
}