package com.kms.tripplanning.utils;

import org.springframework.stereotype.Component;

import com.kms.tripplanning.entity.Destination;
import com.kms.tripplanning.utils.Impl.DestinationFilterRepositoryImpl;
import com.kms.tripplanning.utils.Impl.GenericFilterRepositoryImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class GenericFilterFactory {
    
    @PersistenceContext
    private EntityManager em;

    public <T> GenericFilterRepository<T> create(Class<T> entityClass) {

        if (entityClass.equals(Destination.class)) {
            return (GenericFilterRepository<T>) new DestinationFilterRepositoryImpl(em);
        }

        return new GenericFilterRepositoryImpl<>(em, entityClass);
    }
}
