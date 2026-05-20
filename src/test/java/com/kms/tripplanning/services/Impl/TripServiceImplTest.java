package com.kms.tripplanning.services.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.kms.tripplanning.config.AuthUserDetails;
import com.kms.tripplanning.dto.trip.RequestTrip.TripCreate;
import com.kms.tripplanning.dto.trip.RequestTrip.TripUpdate;
import com.kms.tripplanning.dto.trip.ResponseTrip.TripDetail;
import com.kms.tripplanning.entity.Category;
import com.kms.tripplanning.entity.Destination;
import com.kms.tripplanning.entity.Trip;
import com.kms.tripplanning.entity.User;
import com.kms.tripplanning.exception.types.NotFoundException;
import com.kms.tripplanning.dto.trip.TripMapper;
import com.kms.tripplanning.repository.DestinationRepository;
import com.kms.tripplanning.repository.TripRepository;
import com.kms.tripplanning.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private TripMapper tripMapper;

    @InjectMocks
    private TripServiceImpl tripService;

    private Trip sampleTrip;
    private Destination sampleDestination;
    private UUID tripId;
    private UUID destinationId;
    private UUID userId;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        destinationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        Category cat = Category.builder().id(UUID.randomUUID()).name("Beach").build();
        sampleDestination = Destination.builder()
                .id(destinationId)
                .name("Vung Tau")
                .city("Vung Tau")
                .country("Vietnam")
                .rating(4.5f)
                .latitude(10.35)
                .longitude(107.08)
                .categories(Set.of(cat))
                .build();

        sampleTrip = Trip.builder()
                .id(tripId)
                .name("Summer Trip")
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusDays(1))
                .destinations(Set.of(sampleDestination))
                .build();

        lenient().when(tripMapper.toTrip(any())).thenAnswer(inv -> {
            TripCreate req = inv.getArgument(0);
            return Trip.builder()
                    .name(req.getName())
                    .startDate(req.getStartDate())
                    .endDate(req.getEndDate())
                    .build();
        });
        lenient().when(tripMapper.toTripDetail(any())).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            return TripDetail.from(t);
        });
        lenient().doAnswer(inv -> {
            TripUpdate req = inv.getArgument(0);
            Trip t = inv.getArgument(1);
            if (req.getName() != null) t.setName(req.getName());
            if (req.getStartDate() != null) t.setStartDate(req.getStartDate());
            if (req.getEndDate() != null) t.setEndDate(req.getEndDate());
            return null;
        }).when(tripMapper).updateTrip(any(), any());
    }

    @AfterEach
    void tearDown() {
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }

    private AuthUserDetails mockCurrentUser() {
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .password("pass")
                .firstName("John")
                .lastName("Doe")
                .build();
        AuthUserDetails authUser = new AuthUserDetails(user);
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(authUser);
        return authUser;
    }

    // ========== createTrip ==========

    @Test
    void createTrip_shouldSaveAndReturnTripDetail_withoutDestinations() {
        TripCreate request = new TripCreate();
        request.setName("Test Trip");
        request.setStartDate(OffsetDateTime.now());
        request.setEndDate(OffsetDateTime.now().plusDays(1));
        request.setDestinationIds(Collections.emptyList());

        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            if (t.getDestinations() == null)
                t.setDestinations(Set.of());
            return t;
        });

        TripDetail result = tripService.createTrip(request);

        assertThat(result.getName()).isEqualTo("Test Trip");
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldSaveAndReturnTripDetail_withDestinations() {
        TripCreate request = new TripCreate();
        request.setName("Beach Trip");
        request.setStartDate(OffsetDateTime.now());
        request.setEndDate(OffsetDateTime.now().plusDays(1));
        request.setDestinationIds(List.of(destinationId));

        when(destinationRepository.findAllById(List.of(destinationId))).thenReturn(List.of(sampleDestination));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        TripDetail result = tripService.createTrip(request);

        assertThat(result.getName()).isEqualTo("Beach Trip");
        verify(destinationRepository).findAllById(List.of(destinationId));
    }

    @Test
    void createTrip_shouldThrowNotFoundException_whenDestinationIdsInvalid() {
        UUID missingId = UUID.randomUUID();
        TripCreate request = new TripCreate();
        request.setName("Trip");
        request.setStartDate(OffsetDateTime.now());
        request.setEndDate(OffsetDateTime.now().plusDays(1));
        request.setDestinationIds(List.of(destinationId, missingId));

        when(destinationRepository.findAllById(List.of(destinationId, missingId)))
                .thenReturn(List.of(sampleDestination));

        assertThatThrownBy(() -> tripService.createTrip(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("One or more destinations not found");
    }

    @Test
    void createTrip_shouldMapNameAndDatesCorrectly() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = OffsetDateTime.now().plusDays(1);

        TripCreate request = new TripCreate();
        request.setName("Mapped Trip");
        request.setStartDate(start);
        request.setEndDate(end);
        request.setDestinationIds(Collections.emptyList());

        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        when(tripRepository.save(captor.capture())).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            if (t.getDestinations() == null)
                t.setDestinations(Set.of());
            return t;
        });

        tripService.createTrip(request);

        Trip saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Mapped Trip");
        assertThat(saved.getStartDate()).isEqualTo(start);
        assertThat(saved.getEndDate()).isEqualTo(end);
    }

    // ========== updateTrip ==========

    @Test
    void updateTrip_shouldUpdateAllProvidedFields() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(sampleTrip));
        when(tripRepository.save(any(Trip.class))).thenReturn(sampleTrip);

        OffsetDateTime newStart = OffsetDateTime.now();
        OffsetDateTime newEnd = OffsetDateTime.now().plusDays(1);

        TripUpdate request = new TripUpdate();
        request.setName("Updated Trip");
        request.setStartDate(newStart);
        request.setEndDate(newEnd);
        request.setDestinationIds(Collections.emptyList());

        TripDetail result = tripService.updateTrip(request, tripId);

        assertThat(result.getName()).isEqualTo("Updated Trip");
    }

    @Test
    void updateTrip_shouldKeepExistingValues_whenFieldsAreNull() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(sampleTrip));
        when(tripRepository.save(any(Trip.class))).thenReturn(sampleTrip);

        TripUpdate request = new TripUpdate();
        request.setDestinationIds(Collections.emptyList());

        tripService.updateTrip(request, tripId);

        assertThat(sampleTrip.getName()).isEqualTo("Summer Trip");
    }

    @Test
    void updateTrip_shouldUpdateDestinations_whenDestinationIdsProvided() {
        UUID newDestId = UUID.randomUUID();
        Destination newDest = Destination.builder()
                .id(newDestId).name("New Place").city("City").country("VN")
                .categories(Set.of()).build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(sampleTrip));
        when(destinationRepository.findAllById(List.of(newDestId))).thenReturn(List.of(newDest));
        when(tripRepository.save(any(Trip.class))).thenReturn(sampleTrip);

        TripUpdate request = new TripUpdate();
        request.setDestinationIds(List.of(newDestId));

        tripService.updateTrip(request, tripId);

        verify(destinationRepository).findAllById(List.of(newDestId));
    }

    @Test
    void updateTrip_shouldThrowNotFoundException_whenDestinationIdsInvalid() {
        UUID missingId = UUID.randomUUID();
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(sampleTrip));
        when(destinationRepository.findAllById(List.of(missingId))).thenReturn(Collections.emptyList());

        TripUpdate request = new TripUpdate();
        request.setDestinationIds(List.of(missingId));

        assertThatThrownBy(() -> tripService.updateTrip(request, tripId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("One or more destinations not found");
    }

    @Test
    void updateTrip_shouldThrowNotFoundException_whenTripNotFound() {
        UUID randomId = UUID.randomUUID();
        when(tripRepository.findById(randomId)).thenReturn(Optional.empty());

        TripUpdate request = new TripUpdate();

        assertThatThrownBy(() -> tripService.updateTrip(request, randomId))
                .isInstanceOf(NotFoundException.class);
    }

    // ========== deleteTrip ==========

    @Test
    void deleteTrip_shouldMarkDeletedAndSave_whenFound() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(sampleTrip));

        tripService.deleteTrip(tripId);

        assertThat(sampleTrip.isDeleted()).isTrue();
        verify(tripRepository).save(sampleTrip);
    }

    @Test
    void deleteTrip_shouldThrowNotFoundException_whenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(tripRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.deleteTrip(randomId))
                .isInstanceOf(NotFoundException.class);
    }

    // ========== getTripById ==========

    @SuppressWarnings("unchecked")
    @Test
    void getTripById_shouldReturnTripDetail_whenFound() {
        Page<Trip> page = new PageImpl<>(List.of(sampleTrip));
        when(tripRepository.search(any(), any(), any(Set.class))).thenReturn(page);

        TripDetail result = tripService.getTripById(tripId);

        assertThat(result.getName()).isEqualTo("Summer Trip");
    }

    @SuppressWarnings("unchecked")
    @Test
    void getTripById_shouldThrowNotFoundException_whenNotFound() {
        when(tripRepository.search(any(), any(), any(Set.class))).thenReturn(Page.empty());

        UUID randomId = UUID.randomUUID();
        assertThatThrownBy(() -> tripService.getTripById(randomId))
                .isInstanceOf(NotFoundException.class);
    }

    // ========== searchTrips ==========

    @SuppressWarnings("unchecked")
    @Test
    void searchTrips_shouldReturnPageOfTripDetails() {
        mockCurrentUser();
        Page<Trip> page = new PageImpl<>(List.of(sampleTrip));
        Page<TripDetail> detailPage = new PageImpl<>(List.of(TripDetail.from(sampleTrip)));

        when(tripRepository.search(any(), any(Pageable.class), any(Set.class))).thenReturn(page);
        when(tripRepository.castDTO(eq(page), any(Function.class))).thenReturn(detailPage);

        Page<TripDetail> result = tripService.searchTrips(Map.of(), "name", "asc", 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchTrips_shouldAddCreatedByFilterFromCurrentUser() {
        mockCurrentUser();
        when(tripRepository.search(any(), any(Pageable.class), any(Set.class))).thenReturn(Page.empty());
        when(tripRepository.castDTO(any(), any(Function.class))).thenReturn(Page.empty());

        tripService.searchTrips(new HashMap<>(), null, null, 0, 10);

        ArgumentCaptor<Map<String, List<Object>>> filterCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tripRepository).search(filterCaptor.capture(), any(Pageable.class), any(Set.class));

        assertThat(filterCaptor.getValue()).containsKey("createdBy");
        assertThat(filterCaptor.getValue().get("createdBy").get(0).toString()).isEqualTo(userId.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchTrips_shouldLoadDestinationsAndCategoriesRelations() {
        mockCurrentUser();
        when(tripRepository.search(any(), any(Pageable.class), any(Set.class))).thenReturn(Page.empty());
        when(tripRepository.castDTO(any(), any(Function.class))).thenReturn(Page.empty());

        tripService.searchTrips(Map.of(), null, null, 0, 10);

        ArgumentCaptor<Set<String>> relationsCaptor = ArgumentCaptor.forClass(Set.class);
        verify(tripRepository).search(any(), any(Pageable.class), relationsCaptor.capture());

        assertThat(relationsCaptor.getValue()).containsExactly("destinations", "destinations.categories");
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchTrips_shouldReturnEmptyPage_whenNoResults() {
        mockCurrentUser();
        when(tripRepository.search(any(), any(Pageable.class), any(Set.class))).thenReturn(Page.empty());
        when(tripRepository.castDTO(any(), any(Function.class))).thenReturn(Page.empty());

        Page<TripDetail> result = tripService.searchTrips(Map.of(), null, null, 0, 10);

        assertThat(result.getContent()).isEmpty();
    }
}
