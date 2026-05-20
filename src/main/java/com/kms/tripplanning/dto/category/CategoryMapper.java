package com.kms.tripplanning.dto.category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.kms.tripplanning.dto.category.ResponseCategory.CategoryDetail;
import com.kms.tripplanning.entity.Category;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
    CategoryDetail toCategoryDetail(Category category);

    @Mapping(target = "id", ignore = true)
    Category toCategory(RequestCategory.CategoryCreate requestCategoryCreate);

    @Mapping(target = "id", ignore = true)
    void updateCategory(RequestCategory.CategoryUpdate requestCategoryUpdate, @MappingTarget Category category);
}
