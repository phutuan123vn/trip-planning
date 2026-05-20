package com.kms.tripplanning.dto.trip;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kms.tripplanning.dto.trip.RequestTrip.TripCreate;
import com.kms.tripplanning.dto.trip.RequestTrip.TripUpdate;
import com.kms.tripplanning.dto.trip.ResponseTrip.TripDetail;
import com.kms.tripplanning.entity.Trip;

class TripMapperTest {

    private TripMapper tripMapper;

    @BeforeEach
    void setUp() {
        tripMapper = new TripMapperImpl();
    }

    @Test
    void toTripDetail_shouldMapIdAndName() {
        UUID id = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.of(2024, 6, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime end = OffsetDateTime.of(2024, 6, 10, 10, 0, 0, 0, ZoneOffset.UTC);

        Trip trip = Trip.builder()
                .id(id)
                .name("Summer Trip")
                .startDate(start)
                .endDate(end)
                .destinations(Set.of())
                .build();

        TripDetail detail = tripMapper.toTripDetail(trip);

        assertThat(detail.getId()).isEqualTo(id.toString());
        assertThat(detail.getName()).isEqualTo("Summer Trip");
        assertThat(detail.getStartDate()).isEqualTo(start.toString());
        assertThat(detail.getEndDate()).isEqualTo(end.toString());
    }

    @Test
    void toTripDetail_shouldHandleNullDates() {
        Trip trip = Trip.builder()
                .id(UUID.randomUUID())
                .name("No Dates Trip")
                .destinations(Set.of())
                .build();

        TripDetail detail = tripMapper.toTripDetail(trip);

        assertThat(detail.getStartDate()).isNull();
        assertThat(detail.getEndDate()).isNull();
    }

    @Test
    void toTrip_shouldMapNameAndConvertDatesToUTC() {
        OffsetDateTime start = OffsetDateTime.of(2024, 6, 1, 17, 0, 0, 0, ZoneOffset.ofHours(7));
        OffsetDateTime end = OffsetDateTime.of(2024, 6, 10, 17, 0, 0, 0, ZoneOffset.ofHours(7));

        TripCreate request = new TripCreate();
        request.setName("Beach Trip");
        request.setStartDate(start);
        request.setEndDate(end);

        Trip trip = tripMapper.toTrip(request);

        assertThat(trip.getName()).isEqualTo("Beach Trip");
        assertThat(trip.getId()).isNull();
        assertThat(trip.getStartDate().getOffset()).isEqualTo(ZoneOffset.UTC);
        assertThat(trip.getEndDate().getOffset()).isEqualTo(ZoneOffset.UTC);
        // Same instant, different offset
        assertThat(trip.getStartDate().toInstant()).isEqualTo(start.toInstant());
        assertThat(trip.getEndDate().toInstant()).isEqualTo(end.toInstant());
    }

    @Test
    void toTrip_shouldHandleNullDates() {
        TripCreate request = new TripCreate();
        request.setName("No Date Trip");

        Trip trip = tripMapper.toTrip(request);

        assertThat(trip.getStartDate()).isNull();
        assertThat(trip.getEndDate()).isNull();
    }

    @Test
    void updateTrip_shouldUpdateNameAndDates() {
        Trip existing = Trip.builder()
                .id(UUID.randomUUID())
                .name("Old Name")
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusDays(5))
                .destinations(Set.of())
                .build();

        OffsetDateTime newStart = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime newEnd = OffsetDateTime.of(2025, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC);

        TripUpdate update = new TripUpdate();
        update.setName("New Name");
        update.setStartDate(newStart);
        update.setEndDate(newEnd);

        tripMapper.updateTrip(update, existing);

        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getStartDate().toInstant()).isEqualTo(newStart.toInstant());
        assertThat(existing.getEndDate().toInstant()).isEqualTo(newEnd.toInstant());
    }

    @Test
    void updateTrip_shouldIgnoreIdAndDestinationsAndAuditFields() {
        UUID originalId = UUID.randomUUID();
        Trip existing = Trip.builder()
                .id(originalId)
                .name("Original")
                .destinations(Set.of())
                .build();

        TripUpdate update = new TripUpdate();
        update.setName("Updated");

        tripMapper.updateTrip(update, existing);

        assertThat(existing.getId()).isEqualTo(originalId);
        assertThat(existing.getDestinations()).isNotNull();
    }

    @Test
    void updateTrip_shouldKeepExistingDates_whenUpdateDatesAreNull() {
        OffsetDateTime originalStart = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime originalEnd = OffsetDateTime.of(2024, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC);

        Trip existing = Trip.builder()
                .id(UUID.randomUUID())
                .name("Trip")
                .startDate(originalStart)
                .endDate(originalEnd)
                .destinations(Set.of())
                .build();

        TripUpdate update = new TripUpdate();
        update.setName("Updated Name");
        // startDate and endDate are null

        tripMapper.updateTrip(update, existing);

        assertThat(existing.getStartDate()).isEqualTo(originalStart);
        assertThat(existing.getEndDate()).isEqualTo(originalEnd);
    }
}
