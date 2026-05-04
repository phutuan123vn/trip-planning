package com.kms.tripplanning.services.Impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kms.tripplanning.dto.trip.RequestTrip.TripCreate;
import com.kms.tripplanning.dto.trip.RequestTrip.TripUpdate;
import com.kms.tripplanning.dto.trip.ResponseTrip.TripDetail;
import com.kms.tripplanning.entity.Trip;
import com.kms.tripplanning.exception.types.NotFoundException;
import com.kms.tripplanning.repository.DestinationRepository;
import com.kms.tripplanning.repository.TripRepository;
import com.kms.tripplanning.services.TripService;
import com.kms.tripplanning.utils.FilterBuilderHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final DestinationRepository destinationRepository;

    @Override
    @Transactional
    public TripDetail createTrip(TripCreate request) {
        var trip = Trip.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        if (request.getDestinationIds() != null && !request.getDestinationIds().isEmpty()) {
            var destinations = destinationRepository.findAllById(request.getDestinationIds());
            if (destinations.size() != request.getDestinationIds().size()) {
                throw new NotFoundException("One or more destinations not found with provided ids");
            }
            trip.setDestinations(destinations);
        }
        tripRepository.save(trip);
        return TripDetail.from(trip);
    }

    @Override
    public TripDetail updateTrip(TripUpdate request, UUID tripId) {
        var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));

        trip.setName(request.getName() != null ? request.getName() : trip.getName());
        trip.setStartDate(request.getStartDate() != null ? request.getStartDate() : trip.getStartDate());
        trip.setEndDate(request.getEndDate() != null ? request.getEndDate() : trip.getEndDate());

        if (request.getDestinationIds() != null && !request.getDestinationIds().isEmpty()) {
            var destinations = destinationRepository.findAllById(request.getDestinationIds());
            if (destinations.size() != request.getDestinationIds().size()) {
                throw new NotFoundException("One or more destinations not found with provided ids");
            }
            trip.setDestinations(destinations);
        }

        tripRepository.save(trip);

        return TripDetail.from(trip);

    }

    @Override
    public void deleteTrip(UUID tripId) {
        var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));
        trip.markDeleted();
        tripRepository.save(trip);
    }

    @Override
    public TripDetail getTripById(UUID tripId) {
        FilterBuilderHelper filterBuilder = new FilterBuilderHelper();
        filterBuilder.addFilter("id", tripId.toString());
        var trip = tripRepository.search(
                filterBuilder.build(),
                null,
                List.of("destinations", "destinations.categories")).stream().findFirst()
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));
        return TripDetail.from(trip);
    }

    @Override
    public List<TripDetail> searchTrips(
            Map<String, List<String>> filters,
            String sortBy,
            String sortDirection,
            int page,
            int size) {
        FilterBuilderHelper filterBuilder = new FilterBuilderHelper();
        filters.forEach(filterBuilder::addFilter);
        var trips = tripRepository.search(
                filterBuilder.build(),
                PageRequest.of(page, size, FilterBuilderHelper.buildSort(sortBy, sortDirection)),
                List.of("destinations", "destinations.categories"));
        return trips.stream().map(TripDetail::from).toList();
    }
}
