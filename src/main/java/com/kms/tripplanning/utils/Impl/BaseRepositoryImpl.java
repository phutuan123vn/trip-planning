package com.kms.tripplanning.utils.Impl;

import java.util.List;
import java.util.Map;

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

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation,
            EntityManager em, GenericFilterFactory genericFilterFactory) {
        super(entityInformation, em);
        this.genericFilterRepository = genericFilterFactory.<T>create(entityInformation.getJavaType());
    }

    @Override
    public Page<T> search(Map<String, List<Object>> filters, @Nullable Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 10);
        }
        return genericFilterRepository.search(filters, pageable);
    }

    @Override
    public <DTO> List<DTO> castList(List<Object> values, Class<DTO> clazz) {
        return genericFilterRepository.castList(values, clazz);
    }
}
