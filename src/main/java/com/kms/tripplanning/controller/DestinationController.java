package com.kms.tripplanning.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kms.tripplanning.dto.PaginationRequest;
import com.kms.tripplanning.dto.destination.RequestDestination.DestinationCreate;
import com.kms.tripplanning.dto.destination.RequestDestination.DestinationUpdate;
import com.kms.tripplanning.dto.destination.ResponseDestination.DestinationDetail;
import com.kms.tripplanning.services.DestinationService;

import lombok.RequiredArgsConstructor;

@ApiController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @PostMapping("/list")
    public Page<DestinationDetail> list(@RequestBody PaginationRequest request) {
        return destinationService.listDestination(
                request.getFilters(),
                request.getSortBy(),
                request.getSortDirection(),
                request.getPage(),
                request.getSize());
    }

    @PostMapping("/{destinationId}")
    public DestinationDetail update(@RequestBody DestinationUpdate request, @PathVariable UUID destinationId) {
        return destinationService.updateDestination(destinationId, request);
    }

    @PostMapping("/")
    public DestinationDetail create(@RequestBody DestinationCreate request) {
        return destinationService.createDestination(request);
    }

    @DeleteMapping("/{destinationId}")
    public void delete(@PathVariable UUID destinationId) {
        destinationService.deleteDestination(destinationId);
    }

    @GetMapping("/{destinationId}")
    public DestinationDetail getById(@PathVariable UUID destinationId) {
        return destinationService.getDestinationById(destinationId);
    }

}
