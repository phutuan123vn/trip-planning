package com.kms.tripplanning.utils;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GenericFilterRepository<T> {
    Page<T> search(Map<String, List<Object>> filters, List<String> loadRelations, Pageable pageable);

    <DTO> List<DTO> castList(List<Object> values, Class<DTO> clazz);
}