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

        public static CategoryDetail from(Category category) {
            CategoryDetail detail = new CategoryDetail();
            detail.id = category.getId().toString();
            detail.name = category.getName();
            return detail;
        }
    }
}
