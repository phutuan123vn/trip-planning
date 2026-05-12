package com.kms.tripplanning.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

import com.kms.tripplanning.entity.Category;
import com.kms.tripplanning.entity.Destination;
import com.kms.tripplanning.utils.Impl.DestinationFilterRepositoryImpl;
import com.kms.tripplanning.utils.Impl.GenericFilterRepositoryImpl;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class GenericFilterFactoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private RepositoryInformation repositoryInformation;

    private GenericFilterFactory factory;

    @BeforeEach
    void setUp() {
        factory = new GenericFilterFactory();
    }

    @Test
    void testDestinationRepositoryMapping() {
        GenericFilterRepository<Destination> repo = factory.create(Destination.class, entityManager);
        assertThat(repo).isInstanceOf(DestinationFilterRepositoryImpl.class);
    }

    @Test
    void testGenericRepositoryMapping() {
        GenericFilterRepository<Category> repo = factory.create(Category.class, entityManager);
        assertThat(repo).isInstanceOf(GenericFilterRepositoryImpl.class);
    }
}
