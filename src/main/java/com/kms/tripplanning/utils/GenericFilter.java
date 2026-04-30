package com.kms.tripplanning.utils;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GenericFilter<T> {

    Page<T> filter(Map<String, Object> filterBy, Pageable pageable);
}
