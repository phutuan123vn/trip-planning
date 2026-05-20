package com.kms.tripplanning.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, GenericFilterRepository<T> {
  Page<T> search(Map<String, List<Object>> filters);

  Page<T> search(Map<String, List<Object>> filters, Pageable pageable);

  Page<T> search(Map<String, List<Object>> filters, Pageable pageable, List<String> loadRelations);

  Page<T> search(Map<String, List<Object>> filters, List<String> loadRelations);
}
