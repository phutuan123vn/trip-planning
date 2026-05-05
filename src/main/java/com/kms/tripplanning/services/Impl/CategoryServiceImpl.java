package com.kms.tripplanning.services.Impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.kms.tripplanning.dto.category.RequestCategory.CategoryCreate;
import com.kms.tripplanning.dto.category.ResponseCategory.CategoryDetail;
import com.kms.tripplanning.entity.Category;
import com.kms.tripplanning.exception.types.NotFoundException;
import com.kms.tripplanning.repository.CategoryRepository;
import com.kms.tripplanning.services.CategoryService;
import com.kms.tripplanning.utils.FilterBuilderHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Page<CategoryDetail> listCategories(Map<String, List<String>> filters, String sortBy, String sortDirection,
            int page, int size) {
        FilterBuilderHelper filterBuilder = new FilterBuilderHelper();
        filters.forEach(filterBuilder::addFilter);
        var sort = FilterBuilderHelper.buildSort(sortBy, sortDirection);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        var categories = categoryRepository.search(filterBuilder.build(), pageRequest);
        return categoryRepository.castDTO(categories, CategoryDetail::from);
    }

    @Override
    public CategoryDetail getCategoryById(UUID categoryId) {
        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        return CategoryDetail.from(category);
    }

    @Override
    public CategoryDetail createCategory(CategoryCreate request) {
        var category = Category.builder()
                .name(request.getName())
                .build();
        categoryRepository.save(category);
        return CategoryDetail.from(category);
    }

    @Override
    public void deleteCategory(UUID categoryId) {
        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        categoryRepository.delete(category);
    }

    @Override
    public CategoryDetail updateCategory(UUID categoryId, CategoryCreate request) {
        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        category.setName(request.getName());
        categoryRepository.save(category);
        return CategoryDetail.from(category);
    }
}
