package com.kms.tripplanning.utils;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import com.kms.tripplanning.utils.Impl.BaseRepositoryImpl;

import jakarta.persistence.EntityManager;

public class BaseRepositoryFactoryBean<T extends BaseRepository<S, ID>, S, ID>
        extends JpaRepositoryFactoryBean<T, S, ID> {

    private final GenericFilterFactory genericFilterFactory;

    public BaseRepositoryFactoryBean(Class<? extends T> repositoryInterface,
            GenericFilterFactory genericFilterFactory) {
        super(repositoryInterface);
        this.genericFilterFactory = genericFilterFactory;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new BaseRepositoryFactory<>(entityManager, genericFilterFactory);
    }

    /**
     * Custom factory to handle BaseRepositoryImpl instantiation
     */
    public static class BaseRepositoryFactory<T, ID>
            extends JpaRepositoryFactory {

        private final GenericFilterFactory genericFilterFactory;

        public BaseRepositoryFactory(EntityManager entityManager,
                GenericFilterFactory genericFilterFactory) {
            super(entityManager);
            this.genericFilterFactory = genericFilterFactory;
        }

        @Override
        protected JpaRepositoryImplementation<?, ?> getTargetRepository(
                RepositoryInformation information, EntityManager entityManager) {
            return new BaseRepositoryImpl<>(
                    getEntityInformation((Class<?>) information.getDomainType()),
                    entityManager,
                    genericFilterFactory
            );
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return BaseRepositoryImpl.class;
        }
    }
}
