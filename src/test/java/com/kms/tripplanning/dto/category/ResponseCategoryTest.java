package com.kms.tripplanning.dto.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.kms.tripplanning.dto.category.ResponseCategory.CategoryDetail;
import com.kms.tripplanning.entity.Category;

class ResponseCategoryTest {

    private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void toCategoryDetail_shouldMapIdAndName() {
        UUID id = UUID.randomUUID();
        Category category = Category.builder()
                .id(id)
                .name("Beach")
                .build();

        CategoryDetail detail = categoryMapper.toCategoryDetail(category);

        assertThat(detail.getId()).isEqualTo(id.toString());
        assertThat(detail.getName()).isEqualTo("Beach");
    }
}
