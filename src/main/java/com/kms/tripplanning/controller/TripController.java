package com.kms.tripplanning.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.kms.tripplanning.dto.ApiRequest;
import com.kms.tripplanning.dto.trip.RequestTrip.TripCreate;
import com.kms.tripplanning.dto.trip.RequestTrip.TripUpdate;
import com.kms.tripplanning.dto.trip.ResponseTrip.TripDetail;
import com.kms.tripplanning.services.TripService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@ApiController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping("/list")
    public List<TripDetail> list(@RequestBody ApiRequest request) {

        return tripService.searchTrips(
                request.getFilters(),
                request.getSortBy(),
                request.getSortDirection(),
                request.getPage(),
                request.getSize());
    }

    @DeleteMapping("/{tripId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteTrip(@PathVariable String tripId) {
        tripService.deleteTrip(UUID.fromString(tripId));
    }

    @PostMapping("/")
    public TripDetail create(@Valid @RequestBody TripCreate entity) {

        return tripService.createTrip(entity);
    }

    @PostMapping("/{tripId}")
    public TripDetail updateTrip(@PathVariable String tripId, @RequestBody TripUpdate request) {
        return tripService.updateTrip(request, UUID.fromString(tripId));
    }

    @GetMapping("/{tripId}")
    public TripDetail getTrip(@PathVariable String tripId) {
        return tripService.getTripById(UUID.fromString(tripId));
    }

}
