package com.kms.tripplanning.dto.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kms.tripplanning.dto.category.RequestCategory.CategoryCreate;
import com.kms.tripplanning.dto.category.RequestCategory.CategoryUpdate;
import com.kms.tripplanning.dto.category.ResponseCategory.CategoryDetail;
import com.kms.tripplanning.entity.Category;

class CategoryMapperTest {

    private CategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        categoryMapper = new CategoryMapperImpl();
    }

    @Test
    void toCategoryDetail_shouldMapIdAndName() {
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(id).name("Beach").build();

        CategoryDetail detail = categoryMapper.toCategoryDetail(category);

        assertThat(detail.getId()).isEqualTo(id.toString());
        assertThat(detail.getName()).isEqualTo("Beach");
    }

    @Test
    void toCategory_shouldMapNameAndIgnoreId() {
        CategoryCreate request = new CategoryCreate("Mountain");

        Category category = categoryMapper.toCategory(request);

        assertThat(category.getName()).isEqualTo("Mountain");
        assertThat(category.getId()).isNull();
    }

    @Test
    void updateCategory_shouldUpdateName() {
        Category existing = Category.builder().id(UUID.randomUUID()).name("OldName").build();
        CategoryUpdate update = new CategoryUpdate("NewName");

        categoryMapper.updateCategory(update, existing);

        assertThat(existing.getName()).isEqualTo("NewName");
    }

    @Test
    void toCategoryDetail_shouldHandleNullId() {
        Category category = Category.builder().name("NullId").build();

        CategoryDetail detail = categoryMapper.toCategoryDetail(category);

        assertThat(detail.getId()).isNull();
        assertThat(detail.getName()).isEqualTo("NullId");
    }
}
