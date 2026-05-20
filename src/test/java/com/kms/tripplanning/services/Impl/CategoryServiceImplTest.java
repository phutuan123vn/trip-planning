package com.kms.tripplanning.services.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.function.Function;

import com.kms.tripplanning.dto.category.CategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.kms.tripplanning.dto.category.RequestCategory.CategoryCreate;
import com.kms.tripplanning.dto.category.RequestCategory.CategoryUpdate;
import com.kms.tripplanning.dto.category.ResponseCategory.CategoryDetail;
import com.kms.tripplanning.entity.Category;
import com.kms.tripplanning.exception.types.NotFoundException;
import com.kms.tripplanning.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category sampleCategory;
    private UUID sampleId;

    @BeforeEach
    void setUp() {
        sampleId = UUID.randomUUID();
        sampleCategory = Category.builder()
                .id(sampleId)
                .name("Beach")
                .build();

        lenient().when(categoryMapper.toCategory(any())).thenAnswer(inv -> {
            CategoryCreate req = inv.getArgument(0);
            return Category.builder().name(req.getName()).build();
        });
        lenient().when(categoryMapper.toCategoryDetail(any())).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            return CategoryDetail.from(c);
        });
        lenient().doAnswer(inv -> {
            CategoryUpdate req = inv.getArgument(0);
            Category c = inv.getArgument(1);
            c.setName(req.getName());
            return null;
        }).when(categoryMapper).updateCategory(any(), any());
    }

    // ========== listCategories ==========

    @Test
    void listCategories_shouldReturnPageOfCategories() {
        Page<Category> categoryPage = new PageImpl<>(List.of(sampleCategory));
        Page<CategoryDetail> detailPage = new PageImpl<>(List.of(CategoryDetail.from(sampleCategory)));

        when(categoryRepository.search(any(), any(Pageable.class))).thenReturn(categoryPage);
        when(categoryRepository.castDTO(eq(categoryPage), any(Function.class))).thenReturn(detailPage);

        Map<String, List<String>> filters = Map.of("name", List.of("Beach"));
        Page<CategoryDetail> result = categoryService.listCategories(filters, "name", "asc", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Beach");
    }

    @Test
    void listCategories_shouldReturnEmptyPage_whenNoResults() {
        Page<Category> emptyPage = Page.empty();
        Page<CategoryDetail> emptyDetailPage = Page.empty();

        when(categoryRepository.search(any(), any(Pageable.class))).thenReturn(emptyPage);
        when(categoryRepository.castDTO(eq(emptyPage), any(Function.class))).thenReturn(emptyDetailPage);

        Map<String, List<String>> filters = Map.of("name", List.of("NonExistent"));
        Page<CategoryDetail> result = categoryService.listCategories(filters, "name", "asc", 0, 10);

        assertThat(result.getContent()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void listCategories_shouldApplyFiltersCorrectly() {
        Page<Category> categoryPage = Page.empty();
        when(categoryRepository.search(any(), any(Pageable.class))).thenReturn(categoryPage);
        when(categoryRepository.castDTO(any(), any(Function.class))).thenReturn(Page.empty());

        Map<String, List<String>> filters = new HashMap<>();
        filters.put("name", List.of("Beach"));
        filters.put("id", List.of(sampleId.toString()));

        categoryService.listCategories(filters, null, null, 0, 10);

        ArgumentCaptor<Map<String, List<Object>>> filterCaptor = ArgumentCaptor.forClass(Map.class);
        verify(categoryRepository).search(filterCaptor.capture(), any(Pageable.class));

        Map<String, List<Object>> capturedFilters = filterCaptor.getValue();
        assertThat(capturedFilters).containsKey("name");
        assertThat(capturedFilters).containsKey("id");
    }

    @SuppressWarnings("unchecked")
    @Test
    void listCategories_shouldApplySortCorrectly() {
        Page<Category> categoryPage = Page.empty();
        when(categoryRepository.search(any(), any(Pageable.class))).thenReturn(categoryPage);
        when(categoryRepository.castDTO(any(), any(Function.class))).thenReturn(Page.empty());

        categoryService.listCategories(Map.of(), "name", "desc", 0, 10);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(categoryRepository).search(any(), pageableCaptor.capture());

        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getSort().getOrderFor("name")).isNotNull();
        assertThat(captured.getSort().getOrderFor("name").getDirection())
                .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
    }

    @Test
    void listCategories_shouldHandleEmptyFilters() {
        Page<Category> categoryPage = Page.empty();
        when(categoryRepository.search(any(), any(Pageable.class))).thenReturn(categoryPage);
        when(categoryRepository.castDTO(any(), any(Function.class))).thenReturn(Page.empty());

        Page<CategoryDetail> result = categoryService.listCategories(Map.of(), null, null, 0, 10);

        assertThat(result).isNotNull();
        verify(categoryRepository).search(any(), any(Pageable.class));
    }

    // ========== getCategoryById ==========

    @Test
    void getCategoryById_shouldReturnCategoryDetail_whenFound() {
        when(categoryRepository.findById(sampleId)).thenReturn(Optional.of(sampleCategory));

        CategoryDetail result = categoryService.getCategoryById(sampleId);

        assertThat(result.getId()).isEqualTo(sampleId.toString());
        assertThat(result.getName()).isEqualTo("Beach");
    }

    @Test
    void getCategoryById_shouldThrowNotFoundException_whenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(categoryRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(randomId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found with id:");
    }

    // ========== createCategory ==========

    @Test
    void createCategory_shouldSaveAndReturnCategoryDetail() {
        CategoryCreate request = new CategoryCreate("Mountain");

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setId(UUID.randomUUID());
            return cat;
        });

        CategoryDetail result = categoryService.createCategory(request);

        assertThat(result.getName()).isEqualTo("Mountain");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_shouldSetNameFromRequest() {
        CategoryCreate request = new CategoryCreate("Adventure");

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        when(categoryRepository.save(captor.capture())).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setId(UUID.randomUUID());
            return cat;
        });

        categoryService.createCategory(request);

        assertThat(captor.getValue().getName()).isEqualTo("Adventure");
    }

    // ========== deleteCategory ==========

    @Test
    void deleteCategory_shouldDeleteCategory_whenFound() {
        when(categoryRepository.findById(sampleId)).thenReturn(Optional.of(sampleCategory));

        categoryService.deleteCategory(sampleId);

        verify(categoryRepository).delete(sampleCategory);
    }

    @Test
    void deleteCategory_shouldThrowNotFoundException_whenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(categoryRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(randomId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found with id:");
    }

    // ========== updateCategory ==========

    @Test
    void updateCategory_shouldUpdateNameAndReturnDetail() {
        when(categoryRepository.findById(sampleId)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);

        CategoryUpdate request = new CategoryUpdate("Updated Name");
        CategoryDetail result = categoryService.updateCategory(sampleId, request);

        assertThat(result.getName()).isEqualTo("Updated Name");
        verify(categoryRepository).save(sampleCategory);
    }

    @Test
    void updateCategory_shouldThrowNotFoundException_whenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(categoryRepository.findById(randomId)).thenReturn(Optional.empty());

        CategoryUpdate request = new CategoryUpdate("Updated Name");

        assertThatThrownBy(() -> categoryService.updateCategory(randomId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found with id:");
    }
}
