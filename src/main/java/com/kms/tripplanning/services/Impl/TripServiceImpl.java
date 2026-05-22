package com.kms.tripplanning.services.Impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kms.tripplanning.dto.trip.TripMapper;
import com.kms.tripplanning.dto.trip.RequestTrip.TripCreate;
import com.kms.tripplanning.dto.trip.RequestTrip.TripUpdate;
import com.kms.tripplanning.dto.trip.ResponseTrip.TripDetail;
import com.kms.tripplanning.exception.types.NotFoundException;
import com.kms.tripplanning.repository.DestinationRepository;
import com.kms.tripplanning.repository.TripRepository;
import com.kms.tripplanning.services.TripService;
import com.kms.tripplanning.utils.FilterBuilderHelper;
import com.kms.tripplanning.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final DestinationRepository destinationRepository;
    private final TripMapper tripMapper;    

    @Override
    @Transactional
    public TripDetail createTrip(TripCreate request) {
        var trip = tripMapper.toTrip(request);
        if (request.getDestinationIds() != null && !request.getDestinationIds().isEmpty()) {
            var destinations = destinationRepository.findAllById(request.getDestinationIds());
            if (destinations.size() != request.getDestinationIds().size()) {
                throw new NotFoundException("One or more destinations not found with provided ids");
            }
            trip.setDestinations(new java.util.HashSet<>(destinations));
        }
        tripRepository.save(trip);
        return tripMapper.toTripDetail(trip);
    }

    @Override
    public TripDetail updateTrip(TripUpdate request, UUID tripId) {
        var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));

        tripMapper.updateTrip(request, trip);

        if (request.getDestinationIds() != null && !request.getDestinationIds().isEmpty()) {
            var destinations = destinationRepository.findAllById(request.getDestinationIds());
            if (destinations.size() != request.getDestinationIds().size()) {
                throw new NotFoundException("One or more destinations not found with provided ids");
            }
            trip.setDestinations(new java.util.HashSet<>(destinations));
        }

        tripRepository.save(trip);

        return tripMapper.toTripDetail(trip);
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
        List<String> relations = List.of("destinations", "destinations.categories");
        var trip = tripRepository.search(
                filterBuilder.build(),
                null,
            relations).stream().findFirst()
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + tripId));
        return tripMapper.toTripDetail(trip);
    }

    @Override
    public Page<TripDetail> searchTrips(
            Map<String, List<String>> filters,
            String sortBy,
            String sortDirection,
            int page,
            int size) {
        var user = SecurityUtils.getCurrentUser();
        FilterBuilderHelper filterBuilder = new FilterBuilderHelper();
        filters.forEach(filterBuilder::addFilter);
        filterBuilder.addFilter("createdBy", user.getId().toString());
        List<String> relations = List.of("destinations", "destinations.categories");
        var trips = tripRepository.search(
                filterBuilder.build(),
                PageRequest.of(page, size, FilterBuilderHelper.buildSort(sortBy, sortDirection)),
            relations);
        return tripRepository.castDTO(trips, tripMapper::toTripDetail);
    }
}
