package com.kms.tripplanning.utils;

import org.springframework.stereotype.Component;

import com.kms.tripplanning.entity.Destination;
import com.kms.tripplanning.utils.Impl.DestinationFilterRepositoryImpl;
import com.kms.tripplanning.utils.Impl.GenericFilterRepositoryImpl;

import jakarta.persistence.EntityManager;

@Component
public class GenericFilterFactory {

    public <T> GenericFilterRepository<T> create(Class<T> entityClass, EntityManager em) {

        if (entityClass.equals(Destination.class)) {
            return (GenericFilterRepository<T>) new DestinationFilterRepositoryImpl(em);
        }

        return new GenericFilterRepositoryImpl<>(em, entityClass);
    }
}
