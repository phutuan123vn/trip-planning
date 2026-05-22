package com.kms.tripplanning.dto.category;

import com.kms.tripplanning.entity.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ResponseCategory {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDetail {
        private String id;
        private String name;
        
    }
}
