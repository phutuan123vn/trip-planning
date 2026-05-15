package com.kms.tripplanning.dto.trip;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RequestTrip {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TripCreate {

        @NotEmpty(message = "Name must not be empty")
        private String name;

        @NotNull(message = "Start date must not be empty")
        private Date startDate;

        @NotNull(message = "End date must not be empty")
        private Date endDate;


        @Builder.Default
        private List<UUID> destinationIds = Collections.emptyList();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TripUpdate {
        private String name;
        private Date startDate;
        private Date endDate;
        private List<UUID> destinationIds = Collections.emptyList();
    }

}
