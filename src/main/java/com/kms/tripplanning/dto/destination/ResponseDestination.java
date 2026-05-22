package com.kms.tripplanning.dto.destination;

import java.util.List;

import com.kms.tripplanning.dto.category.ResponseCategory.CategoryDetail;
import com.kms.tripplanning.entity.Destination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ResponseDestination {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DestinationDetail {
        private String id;
        private String name;
        private String city;
        private String country;
        private float rating;
        private List<CategoryDetail> categories;
        private double latitude;
        private double longitude;
        private String thumbnailUrl;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DestinationDropdown {
        private String id;
        private String name;
        private String city;
        private String country;
        private float rating;
    }
}
