package com.kms.tripplanning.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kms.tripplanning.dto.PaginationRequest;
import com.kms.tripplanning.dto.category.RequestCategory.CategoryCreate;
import com.kms.tripplanning.dto.category.ResponseCategory.CategoryDetail;
import com.kms.tripplanning.services.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@ApiController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    @PostMapping("/list")
    public Page<CategoryDetail> list(@RequestBody PaginationRequest request) {
        return categoryService.listCategories(
                request.getFilters(),
                request.getSortBy(),
                request.getSortDirection(),
                request.getPage(),
                request.getSize());
    }

    @GetMapping("/{categoryId}")
    public CategoryDetail getById(@PathVariable UUID id) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping("/")
    public CategoryDetail create(@RequestBody CategoryCreate request) {
        return categoryService.createCategory(request);
    }

    @PostMapping("/{categoryId}")
    public CategoryDetail update(@PathVariable UUID categoryId, @RequestBody CategoryCreate request) {
        return categoryService.updateCategory(categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    public void delete(@PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);
    }
    
}
