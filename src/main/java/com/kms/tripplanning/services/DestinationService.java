package com.kms.tripplanning.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.kms.tripplanning.dto.destination.RequestDestination.DestinationCreate;
import com.kms.tripplanning.dto.destination.RequestDestination.DestinationUpdate;
import com.kms.tripplanning.dto.destination.ResponseDestination.DestinationDetail;

public interface DestinationService {

    Page<DestinationDetail> listDestination(
            Map<String, List<String>> filters,
            String sortBy,
            String sortDirection,
            int page,
            int size);

    DestinationDetail getDestinationById(UUID destinationId);

    DestinationDetail createDestination(DestinationCreate request);

    void deleteDestination(UUID destinationId);

    DestinationDetail updateDestination(UUID destinationId, DestinationUpdate request);

}
