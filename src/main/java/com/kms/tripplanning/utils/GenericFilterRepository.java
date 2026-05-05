package com.kms.tripplanning.utils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GenericFilterRepository<T> {
    Page<T> search(Map<String, List<Object>> filters, Pageable pageable, List<String> loadRelations);

    <S, T> Page<T> castDTO(Page<S> values, Function<S, T> mapper);
}