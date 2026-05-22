package com.kms.tripplanning.dto.trip;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.kms.tripplanning.dto.trip.ResponseTrip.TripDetail;
import com.kms.tripplanning.entity.Destination;
import com.kms.tripplanning.entity.Trip;

class ResponseTripTest {

    private final TripMapper tripMapper = Mappers.getMapper(TripMapper.class);

    @Test
    void toTripDetail_shouldMapAllFields() {
        UUID tripId = UUID.randomUUID();
        UUID destId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = OffsetDateTime.now().plusDays(1);

        Destination destination = Destination.builder()
                .id(destId)
                .name("Vung Tau")
                .categories(Set.of())
                .build();

        Trip trip = Trip.builder()
                .id(tripId)
                .name("Summer Trip")
                .startDate(start)
                .endDate(end)
                .destinations(Set.of(destination))
                .build();

        TripDetail detail = tripMapper.toTripDetail(trip);

        assertThat(detail.getId()).isEqualTo(tripId.toString());
        assertThat(detail.getName()).isEqualTo("Summer Trip");
        assertThat(detail.getStartDate()).isEqualTo(start.toString());
        assertThat(detail.getEndDate()).isEqualTo(end.toString());

        assertThat(detail.getDestinations()).hasSize(1);
        assertThat(detail.getDestinations().get(0).getId()).isEqualTo(destId.toString());
        assertThat(detail.getDestinations().get(0).getName()).isEqualTo("Vung Tau");
    }
}
