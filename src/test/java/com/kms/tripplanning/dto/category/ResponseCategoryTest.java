package com.kms.tripplanning.dto.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.kms.tripplanning.dto.category.ResponseCategory.CategoryDetail;
import com.kms.tripplanning.entity.Category;

class ResponseCategoryTest {

    @Test
    void from_shouldMapIdAndName() {
        UUID id = UUID.randomUUID();
        Category category = Category.builder()
                .id(id)
                .name("Beach")
                .build();

        CategoryDetail detail = CategoryDetail.from(category);

        assertThat(detail.getId()).isEqualTo(id.toString());
        assertThat(detail.getName()).isEqualTo("Beach");
    }
}
