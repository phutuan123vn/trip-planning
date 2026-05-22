package com.kms.tripplanning.dto.trip;

import java.util.Collections;
import java.util.List;

import com.kms.tripplanning.dto.destination.ResponseDestination.DestinationDetail;
import com.kms.tripplanning.entity.Trip;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ResponseTrip {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TripDetail {
        private String id;
        private String name;
        private String startDate;
        private String endDate;
        private List<DestinationDetail> destinations = Collections.emptyList();
        
    }
}
