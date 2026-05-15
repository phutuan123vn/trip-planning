package com.kms.tripplanning.services.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.kms.tripplanning.dto.destination.RequestDestination.DestinationCreate;
import com.kms.tripplanning.dto.destination.RequestDestination.DestinationUpdate;
import com.kms.tripplanning.dto.destination.ResponseDestination.DestinationDetail;
import com.kms.tripplanning.entity.Category;
import com.kms.tripplanning.entity.Destination;
import com.kms.tripplanning.exception.types.NotFoundException;
import com.kms.tripplanning.repository.CategoryRepository;
import com.kms.tripplanning.repository.DestinationRepository;

@ExtendWith(MockitoExtension.class)
class DestinationServiceImplTest {

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DestinationServiceImpl destinationService;

    private Destination sampleDestination;
    private Category sampleCategory;
    private UUID destinationId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        destinationId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        sampleCategory = Category.builder().id(categoryId).name("Beach").build();
        sampleDestination = Destination.builder()
                .id(destinationId)
                .name("Vung Tau")
                .city("Vung Tau")
                .country("Vietnam")
                .rating(4.5f)
                .latitude(10.35)
                .longtitude(107.08)
                .thumbnailUrl("https://example.com/img.jpg")
                .categories(Set.of(sampleCategory))
                .build();
    }

    // ========== listDestination ==========

    @Test
    void listDestination_shouldReturnPageOfDestinationDetails() {
        Page<Destination> page = new PageImpl<>(List.of(sampleDestination));
        Page<DestinationDetail> detailPage = new PageImpl<>(List.of(DestinationDetail.from(sampleDestination)));

        when(destinationRepository.search(any(), any(Pageable.class), any(Set.class))).thenReturn(page);
        when(destinationRepository.castDTO(eq(page), any(Function.class))).thenReturn(detailPage);

        Map<String, List<String>> filters = Map.of("city", List.of("Vung Tau"));
        Page<DestinationDetail> result = destinationService.listDestination(filters, "name", "asc", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Vung Tau");
    }

    @Test
    void listDestination_shouldReturnEmptyPage_whenNoResults() {
        when(destinationRepository.search(any(), any(Pageable.class), any(Set.class))).thenReturn(Page.empty());
        when(destinationRepository.castDTO(any(), any(Function.class))).thenReturn(Page.empty());

        Page<DestinationDetail> result = destinationService.listDestination(Map.of(), null, null, 0, 10);

        assertThat(result.getContent()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void listDestination_shouldPassCategoriesAsLoadRelation() {
        when(destinationRepository.search(any(), any(Pageable.class), any(Set.class))).thenReturn(Page.empty());
        when(destinationRepository.castDTO(any(), any(Function.class))).thenReturn(Page.empty());

        destinationService.listDestination(Map.of(), null, null, 0, 10);

        ArgumentCaptor<Set<String>> relationsCaptor = ArgumentCaptor.forClass(Set.class);
        verify(destinationRepository).search(any(), any(Pageable.class), relationsCaptor.capture());

        assertThat(relationsCaptor.getValue()).containsExactly("categories");
    }

    @Test
    void listDestination_shouldApplySortCorrectly() {
        when(destinationRepository.search(any(), any(Pageable.class), any(Set.class))).thenReturn(Page.empty());
        when(destinationRepository.castDTO(any(), any(Function.class))).thenReturn(Page.empty());

        destinationService.listDestination(Map.of(), "rating", "desc", 0, 10);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(destinationRepository).search(any(), pageableCaptor.capture(), any(Set.class));

        assertThat(pageableCaptor.getValue().getSort().getOrderFor("rating")).isNotNull();
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("rating").getDirection())
                .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
    }

    // ========== getDestinationById ==========

    @Test
    void getDestinationById_shouldReturnDestinationDetail_whenFound() {
        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(sampleDestination));

        DestinationDetail result = destinationService.getDestinationById(destinationId);

        assertThat(result.getId()).isEqualTo(destinationId.toString());
        assertThat(result.getName()).isEqualTo("Vung Tau");
        assertThat(result.getCity()).isEqualTo("Vung Tau");
        assertThat(result.getCountry()).isEqualTo("Vietnam");
    }

    @Test
    void getDestinationById_shouldThrowNotFoundException_whenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(destinationRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> destinationService.getDestinationById(randomId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Destionation not Found");
    }

    // ========== createDestination ==========

    @Test
    void createDestination_shouldSaveAndReturnDetail_withoutCategories() {
        DestinationCreate request = new DestinationCreate();
        request.setName("Da Lat");
        request.setCity("Da Lat");
        request.setCountry("Vietnam");
        request.setLatitude(11.94);
        request.setLongitude(108.44);
        request.setRating(4.0f);
        request.setCategoryIds(Collections.emptyList());

        when(destinationRepository.save(any(Destination.class))).thenAnswer(inv -> {
            Destination d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            if (d.getCategories() == null) d.setCategories(Set.of());
            return d;
        });

        DestinationDetail result = destinationService.createDestination(request);

        assertThat(result.getName()).isEqualTo("Da Lat");
        verify(destinationRepository).save(any(Destination.class));
    }

    @Test
    void createDestination_shouldSaveAndReturnDetail_withCategories() {
        DestinationCreate request = new DestinationCreate();
        request.setName("Da Lat");
        request.setCity("Da Lat");
        request.setCountry("Vietnam");
        request.setLatitude(11.94);
        request.setLongitude(108.44);
        request.setRating(4.0f);
        request.setCategoryIds(List.of(categoryId));

        when(categoryRepository.findAllById(List.of(categoryId))).thenReturn(List.of(sampleCategory));
        when(destinationRepository.save(any(Destination.class))).thenAnswer(inv -> {
            Destination d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DestinationDetail result = destinationService.createDestination(request);

        assertThat(result.getName()).isEqualTo("Da Lat");
        verify(categoryRepository).findAllById(List.of(categoryId));
    }

    @Test
    void createDestination_shouldThrowNotFoundException_whenCategoryIdsInvalid() {
        UUID missingCatId = UUID.randomUUID();
        DestinationCreate request = new DestinationCreate();
        request.setName("Test");
        request.setCity("City");
        request.setCountry("Country");
        request.setLatitude(10.0);
        request.setLongitude(106.0);
        request.setRating(3.0f);
        request.setCategoryIds(List.of(categoryId, missingCatId));

        when(categoryRepository.findAllById(List.of(categoryId, missingCatId)))
                .thenReturn(List.of(sampleCategory)); // returns only 1 of 2

        assertThatThrownBy(() -> destinationService.createDestination(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("One or more categories not found");
    }

    @Test
    void createDestination_shouldMapAllFieldsCorrectly() {
        DestinationCreate request = new DestinationCreate();
        request.setName("Phu Quoc");
        request.setCity("Kien Giang");
        request.setCountry("Vietnam");
        request.setLatitude(10.22);
        request.setLongitude(103.96);
        request.setRating(4.8f);
        request.setThumbnailUrl("https://example.com/pq.jpg");
        request.setCategoryIds(Collections.emptyList());

        ArgumentCaptor<Destination> captor = ArgumentCaptor.forClass(Destination.class);
        when(destinationRepository.save(captor.capture())).thenAnswer(inv -> {
            Destination d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            if (d.getCategories() == null) d.setCategories(Set.of());
            return d;
        });

        destinationService.createDestination(request);

        Destination saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Phu Quoc");
        assertThat(saved.getCity()).isEqualTo("Kien Giang");
        assertThat(saved.getCountry()).isEqualTo("Vietnam");
        assertThat(saved.getLatitude()).isEqualTo(10.22);
        assertThat(saved.getLongtitude()).isEqualTo(103.96);
        assertThat(saved.getRating()).isEqualTo(4.8f);
        assertThat(saved.getThumbnailUrl()).isEqualTo("https://example.com/pq.jpg");
    }

    // ========== deleteDestination ==========

    @Test
    void deleteDestination_shouldDeleteDestination_whenFound() {
        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(sampleDestination));

        destinationService.deleteDestination(destinationId);

        verify(destinationRepository).delete(sampleDestination);
    }

    @Test
    void deleteDestination_shouldThrowNotFoundException_whenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(destinationRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> destinationService.deleteDestination(randomId))
                .isInstanceOf(NotFoundException.class);
    }

    // ========== updateDestination ==========

    @Test
    void updateDestination_shouldUpdateAllProvidedFields() {
        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(sampleDestination));
        when(destinationRepository.save(any(Destination.class))).thenReturn(sampleDestination);

        DestinationUpdate request = new DestinationUpdate();
        request.setName("Updated Name");
        request.setCity("Updated City");
        request.setCountry("Updated Country");
        request.setRating(5.0f);
        request.setLatitude(11.0);
        request.setLongitude(108.0);
        request.setThumbnailUrl("https://example.com/updated.jpg");
        request.setCategoryIds(Collections.emptyList());

        DestinationDetail result = destinationService.updateDestination(destinationId, request);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getCity()).isEqualTo("Updated City");
    }

    @Test
    void updateDestination_shouldKeepExistingValues_whenFieldsAreNull() {
        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(sampleDestination));
        when(destinationRepository.save(any(Destination.class))).thenReturn(sampleDestination);

        DestinationUpdate request = new DestinationUpdate();
        // all fields null except categoryIds which defaults to empty list
        request.setCategoryIds(Collections.emptyList());

        destinationService.updateDestination(destinationId, request);

        // Should keep original values
        assertThat(sampleDestination.getName()).isEqualTo("Vung Tau");
        assertThat(sampleDestination.getCity()).isEqualTo("Vung Tau");
    }

    @Test
    void updateDestination_shouldUpdateCategories_whenCategoryIdsProvided() {
        UUID newCatId = UUID.randomUUID();
        Category newCat = Category.builder().id(newCatId).name("Mountain").build();

        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(sampleDestination));
        when(categoryRepository.findAllById(List.of(newCatId))).thenReturn(List.of(newCat));
        when(destinationRepository.save(any(Destination.class))).thenReturn(sampleDestination);

        DestinationUpdate request = new DestinationUpdate();
        request.setCategoryIds(List.of(newCatId));

        destinationService.updateDestination(destinationId, request);

        verify(categoryRepository).findAllById(List.of(newCatId));
    }

    @Test
    void updateDestination_shouldThrowNotFoundException_whenCategoryIdsInvalid() {
        UUID missingCatId = UUID.randomUUID();
        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(sampleDestination));
        when(categoryRepository.findAllById(List.of(missingCatId))).thenReturn(Collections.emptyList());

        DestinationUpdate request = new DestinationUpdate();
        request.setCategoryIds(List.of(missingCatId));

        assertThatThrownBy(() -> destinationService.updateDestination(destinationId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("One or more categories not found");
    }

    @Test
    void updateDestination_shouldThrowNotFoundException_whenDestinationNotFound() {
        UUID randomId = UUID.randomUUID();
        when(destinationRepository.findById(randomId)).thenReturn(Optional.empty());

        DestinationUpdate request = new DestinationUpdate();

        assertThatThrownBy(() -> destinationService.updateDestination(randomId, request))
                .isInstanceOf(NotFoundException.class);
    }
}
