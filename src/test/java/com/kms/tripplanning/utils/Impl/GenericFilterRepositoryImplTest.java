package com.kms.tripplanning.utils.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.kms.tripplanning.entity.Category;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class GenericFilterRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private JPAQueryFactory queryFactory;

    private JPAQuery<UUID> idQuery;

    private JPAQuery<Category> contentQuery;

    private JPAQuery<Long> countQuery;

    private GenericFilterRepositoryImpl<Category> repository;

    @BeforeEach
    void setUp() {
        idQuery = mock(JPAQuery.class, Mockito.RETURNS_SELF);
        contentQuery = mock(JPAQuery.class, Mockito.RETURNS_SELF);
        countQuery = mock(JPAQuery.class, Mockito.RETURNS_SELF);
        repository = new GenericFilterRepositoryImpl<>(queryFactory, Category.class);
    }

    // ========== castDTO Tests ==========

    @Test
    void castDTO_shouldConvertEntityPageToDtoPage() {
        Category category = Category.builder().id(UUID.randomUUID()).name("Beach").build();
        Page<Category> entityPage = new PageImpl<>(List.of(category), PageRequest.of(0, 10), 1);

        Function<Category, String> mapper = Category::getName;

        Page<String> result = repository.castDTO(entityPage, mapper);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo("Beach");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void castDTO_shouldHandleEmptyPage() {
        Page<Category> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        Function<Category, String> mapper = Category::getName;

        Page<String> result = repository.castDTO(emptyPage, mapper);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void castDTO_shouldPreserveSort() {
        Category category1 = Category.builder().id(UUID.randomUUID()).name("Beach").build();
        Category category2 = Category.builder().id(UUID.randomUUID()).name("Mountain").build();
        List<Category> categories = Arrays.asList(category1, category2);

        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<Category> entityPage = new PageImpl<>(categories, pageable, 0);

        Function<Category, String> mapper = Category::getName;

        Page<String> result = repository.castDTO(entityPage, mapper);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // ========== search() Tests ==========

    @Test
    void search_withNullFilters_shouldReturnAllItems() {
        PageRequest pageable = PageRequest.of(0, 10);
        stubQuerydslIdsOnly(List.of());

        Page<Category> result = repository.search(null, pageable, null, null);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void search_withEmptyFilters_shouldReturnAllItems() {
        PageRequest pageable = PageRequest.of(0, 10);
        stubQuerydslIdsOnly(List.of());

        Page<Category> result = repository.search(Collections.emptyMap(), pageable, null, null);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void search_withFiltersAndPageable_shouldHandlePagination() {
        PageRequest pageable = PageRequest.of(0, 10);
        stubQuerydslIdsOnly(List.of());

        Page<Category> result = repository.search(Map.of(), pageable, null, null);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void search_withLoadRelations_shouldHandleEmptyRelationList() {
        List<String> loadRelations = Collections.emptyList();

        PageRequest pageable = PageRequest.of(0, 10);
        stubQuerydslIdsOnly(List.of());

        Page<Category> result = repository.search(Map.of(), pageable, loadRelations, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void search_withAllowedFilterFields_shouldHandleNullAllowlist() {
        List<String> loadRelations = Collections.emptyList();

        PageRequest pageable = PageRequest.of(0, 10);
        stubQuerydslIdsOnly(List.of());

        Page<Category> result = repository.search(Map.of(), pageable, loadRelations, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void search_withAllowedFilterFields_shouldHandleEmptyAllowlist() {
        List<String> loadRelations = Collections.emptyList();

        PageRequest pageable = PageRequest.of(0, 10);
        stubQuerydslIdsOnly(List.of());

        Page<Category> result = repository.search(Map.of(), pageable, loadRelations, Set.of());

        assertThat(result.getContent()).isEmpty();
    }

    // ========== Error Cases ==========

    @Test
    void search_withNullEntityClass_shouldThrowException() {
        assertThatThrownBy(() -> new GenericFilterRepositoryImpl<>(queryFactory, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void search_withNullPageable_shouldThrowException() {
        assertThatThrownBy(() -> repository.search(null, null, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void search_withNullLoadRelations_shouldHandleGracefully() {
        List<String> loadRelations = null;

        PageRequest pageable = PageRequest.of(0, 10);
        stubQuerydslIdsOnly(List.of());

        Page<Category> result = repository.search(Map.of(), pageable, loadRelations, null);

        assertThat(result.getContent()).isEmpty();
    }

    private void stubQuerydslIdsOnly(List<UUID> ids) {
        when(queryFactory.<Object>select(any(Expression.class))).thenReturn((JPAQuery) idQuery);
        when(idQuery.fetch()).thenReturn(ids);
    }

    @SuppressWarnings("unused")
    private void stubQuerydslFullPath(List<UUID> ids, List<Category> content, Long total) {
        // First select() call creates the ID query; second creates the COUNT query.
        when(queryFactory.<Object>select(any(Expression.class)))
                .thenReturn((JPAQuery) idQuery, (JPAQuery) countQuery);
        when(idQuery.fetch()).thenReturn(ids);
        when(queryFactory.selectFrom(any(PathBuilder.class))).thenReturn(contentQuery);
        when(contentQuery.fetch()).thenReturn(content);
        when(countQuery.fetchOne()).thenReturn(total);
    }
}
