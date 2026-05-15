package com.kms.tripplanning.dto.destination;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RequestDestination {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DestinationCreate {

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "Country is required")
        private String country;

        @NotNull(message = "Latitude is required")
        private Double latitude;

        @NotNull(message = "Longitude is required")
        private Double longitude;

        private String thumbnailUrl;

        @NotNull(message = "Rating is required")
        @Builder.Default
        private float rating = 0.0f;


        @Builder.Default
        private List<UUID> categoryIds = Collections.emptyList();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DestinationUpdate {
        private String name;
        private String city;
        private String country;
        private Double latitude;
        private Double longitude;
        private String thumbnailUrl;
        private Float rating;
        private List<UUID> categoryIds = Collections.emptyList();
    }

    
}
