package com.kms.tripplanning.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.kms.tripplanning.dto.trip.RequestTrip.TripCreate;
import com.kms.tripplanning.dto.trip.RequestTrip.TripUpdate;
import com.kms.tripplanning.dto.trip.ResponseTrip.TripDetail;

public interface TripService {

    TripDetail createTrip(TripCreate request);

    TripDetail updateTrip(TripUpdate request, UUID tripId);

    void deleteTrip(UUID tripId);

    TripDetail getTripById(UUID tripId);

    Page<TripDetail> searchTrips(
            Map<String, List<String>> filters,
            String sortBy,
            String sortDirection,
            int page,
            int size);
}
