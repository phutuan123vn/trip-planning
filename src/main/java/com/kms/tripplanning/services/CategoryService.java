package com.kms.tripplanning.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.kms.tripplanning.dto.category.RequestCategory.CategoryCreate;
import com.kms.tripplanning.dto.category.RequestCategory.CategoryUpdate;
import com.kms.tripplanning.dto.category.ResponseCategory.CategoryDetail;

public interface CategoryService {
    
    Page<CategoryDetail> listCategories(
        Map<String, List<String>> filters,
        String sortBy,
        String sortDirection,
        int page,
        int size
    );

    CategoryDetail getCategoryById(UUID categoryId);

    CategoryDetail createCategory(CategoryCreate request);

    void deleteCategory(UUID categoryId);

    CategoryDetail updateCategory(UUID categoryId, CategoryUpdate request);
}
