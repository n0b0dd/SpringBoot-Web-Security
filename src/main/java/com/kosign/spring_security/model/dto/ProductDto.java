package com.kosign.spring_security.model.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ProductDto {
@Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRequest {
        @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
        private String name;

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        private String description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        private BigDecimal price;

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        private Integer stockQuantity;

        @Pattern(regexp = "^(https?://.*|)$", message = "Invalid URL format")
        private String imageUrl;

        @NotNull(message = "Category ID is required")
        private Long categoryId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private String imageUrl;
        private String status;
        private String category;
        private String createdBy;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductUpdateRequest {
        @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
        private String name;

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        private String description;

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        private BigDecimal price;

        @Min(value = 0, message = "Stock quantity cannot be negative")
        private Integer stockQuantity;

        @Pattern(regexp = "^(https?://.*|)$", message = "Invalid URL format")
        private String imageUrl;

        private Long categoryId;
    }
}
