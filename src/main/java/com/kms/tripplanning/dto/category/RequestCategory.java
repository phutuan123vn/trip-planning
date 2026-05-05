package com.kms.tripplanning.dto.category;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RequestCategory {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryCreate {
        
        @NotEmpty(message = "Name is required")
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryUpdate {

        @NotEmpty(message = "Name is required")
        private String name;
    }
}
