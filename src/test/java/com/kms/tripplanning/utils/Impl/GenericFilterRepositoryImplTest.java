package com.kms.tripplanning.utils.Impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.kms.tripplanning.entity.Category;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class GenericFilterRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    private GenericFilterRepositoryImpl<Category> repository;

    @BeforeEach
    void setUp() {
        repository = new GenericFilterRepositoryImpl<>(entityManager, Category.class);
    }

    // ========== castDTO ==========

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
}
