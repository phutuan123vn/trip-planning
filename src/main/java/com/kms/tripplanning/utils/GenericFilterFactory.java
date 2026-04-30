package com.kms.tripplanning.utils;

import org.springframework.stereotype.Component;

import com.kms.tripplanning.utils.Impl.GenericFilterRepositoryImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class GenericFilterFactory {
    
    @PersistenceContext
    private EntityManager em;

    public <T> GenericFilterRepository<T> create(Class<T> entityClass) {
        return new GenericFilterRepositoryImpl<>(em, entityClass);
    }
}
