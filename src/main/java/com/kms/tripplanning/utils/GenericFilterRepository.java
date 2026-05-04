package com.kms.tripplanning.utils;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GenericFilterRepository<T> {
    Page<T> search(Map<String, List<Object>> filters, Pageable pageable, List<String> loadRelations);

    <DTO> List<DTO> castList(List<Object> values, Class<DTO> clazz);
}