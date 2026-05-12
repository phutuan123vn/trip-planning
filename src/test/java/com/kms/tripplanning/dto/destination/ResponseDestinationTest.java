package com.kms.tripplanning.dto.destination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.kms.tripplanning.dto.destination.ResponseDestination.DestinationDetail;
import com.kms.tripplanning.entity.Category;
import com.kms.tripplanning.entity.Destination;

class ResponseDestinationTest {

    @Test
    void from_shouldMapAllFields() {
        UUID destId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        
        Category category = Category.builder()
                .id(catId)
                .name("Beach")
                .build();
                
        Destination destination = Destination.builder()
                .id(destId)
                .name("Vung Tau")
                .city("Vung Tau City")
                .country("Vietnam")
                .rating(4.5f)
                .latitude(10.35)
                .longtitude(107.08)
                .thumbnailUrl("https://img.com/vt.jpg")
                .categories(Set.of(category))
                .build();

        DestinationDetail detail = DestinationDetail.from(destination);

        assertThat(detail.getId()).isEqualTo(destId.toString());
        assertThat(detail.getName()).isEqualTo("Vung Tau");
        assertThat(detail.getCity()).isEqualTo("Vung Tau City");
        assertThat(detail.getCountry()).isEqualTo("Vietnam");
        assertThat(detail.getRating()).isEqualTo(4.5f);
        assertThat(detail.getLatitude()).isEqualTo(10.35);
        assertThat(detail.getLongtitude()).isEqualTo(107.08);
        assertThat(detail.getThumbnailUrl()).isEqualTo("https://img.com/vt.jpg");
        
        assertThat(detail.getCategories()).hasSize(1);
        assertThat(detail.getCategories().get(0).getId()).isEqualTo(catId.toString());
        assertThat(detail.getCategories().get(0).getName()).isEqualTo("Beach");
    }
}
