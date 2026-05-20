package com.kms.tripplanning.dto.destination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kms.tripplanning.dto.destination.RequestDestination.DestinationCreate;
import com.kms.tripplanning.dto.destination.RequestDestination.DestinationUpdate;
import com.kms.tripplanning.dto.destination.ResponseDestination.DestinationDetail;
import com.kms.tripplanning.dto.destination.ResponseDestination.DestinationDropdown;
import com.kms.tripplanning.entity.Category;
import com.kms.tripplanning.entity.Destination;

class DestinationMapperTest {

    private DestinationMapper destinationMapper;

    @BeforeEach
    void setUp() {
        destinationMapper = new DestinationMapperImpl();
    }

    @Test
    void toDestinationDetail_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Category cat = Category.builder().id(catId).name("Beach").build();
        Destination destination = Destination.builder()
                .id(id)
                .name("Vung Tau")
                .city("Vung Tau")
                .country("Vietnam")
                .rating(4.5f)
                .latitude(10.35)
                .longitude(107.08)
                .thumbnailUrl("https://example.com/img.jpg")
                .categories(Set.of(cat))
                .build();

        DestinationDetail detail = destinationMapper.toDestinationDetail(destination);

        assertThat(detail.getId()).isEqualTo(id.toString());
        assertThat(detail.getName()).isEqualTo("Vung Tau");
        assertThat(detail.getCity()).isEqualTo("Vung Tau");
        assertThat(detail.getCountry()).isEqualTo("Vietnam");
        assertThat(detail.getRating()).isEqualTo(4.5f);
        assertThat(detail.getLatitude()).isEqualTo(10.35);
        assertThat(detail.getLongitude()).isEqualTo(107.08);
        assertThat(detail.getThumbnailUrl()).isEqualTo("https://example.com/img.jpg");
        assertThat(detail.getCategories()).hasSize(1);
        assertThat(detail.getCategories().get(0).getName()).isEqualTo("Beach");
    }

    @Test
    void toDestinationDropdown_shouldMapBasicFields() {
        UUID id = UUID.randomUUID();
        Destination destination = Destination.builder()
                .id(id)
                .name("Da Lat")
                .city("Lam Dong")
                .country("Vietnam")
                .rating(4.8f)
                .categories(Set.of())
                .build();

        DestinationDropdown dropdown = destinationMapper.toDestinationDropdown(destination);

        assertThat(dropdown.getId()).isEqualTo(id.toString());
        assertThat(dropdown.getName()).isEqualTo("Da Lat");
        assertThat(dropdown.getCity()).isEqualTo("Lam Dong");
        assertThat(dropdown.getCountry()).isEqualTo("Vietnam");
        assertThat(dropdown.getRating()).isEqualTo(4.8f);
    }

    @Test
    void toDestination_shouldMapFieldsAndIgnoreIdAndCategories() {
        DestinationCreate request = new DestinationCreate();
        request.setName("Phu Quoc");
        request.setCity("Kien Giang");
        request.setCountry("Vietnam");
        request.setLatitude(10.22);
        request.setLongitude(103.96);
        request.setRating(4.8f);
        request.setThumbnailUrl("https://example.com/pq.jpg");

        Destination destination = destinationMapper.toDestination(request);

        assertThat(destination.getId()).isNull();
        assertThat(destination.getCategories()).isNull();
        assertThat(destination.getName()).isEqualTo("Phu Quoc");
        assertThat(destination.getCity()).isEqualTo("Kien Giang");
        assertThat(destination.getCountry()).isEqualTo("Vietnam");
        assertThat(destination.getLatitude()).isEqualTo(10.22);
        assertThat(destination.getLongitude()).isEqualTo(103.96);
        assertThat(destination.getRating()).isEqualTo(4.8f);
        assertThat(destination.getThumbnailUrl()).isEqualTo("https://example.com/pq.jpg");
    }

    @Test
    void updateDestination_shouldUpdateProvidedFields() {
        Destination existing = Destination.builder()
                .id(UUID.randomUUID())
                .name("Old Name")
                .city("Old City")
                .country("Vietnam")
                .rating(3.0f)
                .latitude(10.0)
                .longitude(106.0)
                .categories(Set.of())
                .build();

        DestinationUpdate update = new DestinationUpdate();
        update.setName("New Name");
        update.setCity("New City");
        update.setRating(5.0f);

        destinationMapper.updateDestination(update, existing);

        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getCity()).isEqualTo("New City");
        assertThat(existing.getRating()).isEqualTo(5.0f);
    }

    @Test
    void updateDestination_shouldIgnoreIdAndCategories() {
        UUID originalId = UUID.randomUUID();
        Destination existing = Destination.builder()
                .id(originalId)
                .name("Old Name")
                .categories(Set.of())
                .build();

        DestinationUpdate update = new DestinationUpdate();
        update.setName("New Name");

        destinationMapper.updateDestination(update, existing);

        assertThat(existing.getId()).isEqualTo(originalId);
    }
}
