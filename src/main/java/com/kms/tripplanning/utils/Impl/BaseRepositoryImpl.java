package com.kms.tripplanning.utils.Impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.kms.tripplanning.utils.BaseRepository;
import com.kms.tripplanning.utils.GenericFilterFactory;
import com.kms.tripplanning.utils.GenericFilterRepository;

import jakarta.persistence.EntityManager;

public class BaseRepositoryImpl<T, ID>
    extends SimpleJpaRepository<T, ID>
    implements BaseRepository<T, ID> {

  private final GenericFilterRepository<T> genericFilterRepository;

  public BaseRepositoryImpl(
      JpaEntityInformation<T, ?> entityInformation,
      EntityManager em,
      GenericFilterFactory genericFilterFactory) {
    super(entityInformation, em);
    this.genericFilterRepository = genericFilterFactory.<T>create(
        entityInformation.getJavaType(), em);
  }

  @Override
  public Page<T> search(
      Map<String, List<Object>> filters,
      @Nullable Pageable pageable,
      @Nullable List<String> loadRelations) {
    if (pageable == null) {
      pageable = PageRequest.of(0, 10);
    }
    if (loadRelations == null || loadRelations.isEmpty()) {
      loadRelations = Collections.emptyList();
    }
    return genericFilterRepository.search(filters, pageable, loadRelations);
  }

  @Override
  public <S, T> Page<T> castDTO(Page<S> values, Function<S, T> mapper) {
    return genericFilterRepository.castDTO(values, mapper);
  }

  @Override
  public Page<T> search(Map<String, List<Object>> filters) {
    return search(filters, null, null);
  }

  @Override
  public Page<T> search(Map<String, List<Object>> filters, Pageable pageable) {
    return search(filters, pageable, null);
  }
}
