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
        private double longtitude;
        private String thumbnailUrl;

        public static DestinationDetail from(Destination destination) {
            DestinationDetail detail = new DestinationDetail();
            detail.id = destination.getId().toString();
            detail.name = destination.getName();
            detail.city = destination.getCity();
            detail.country = destination.getCountry();
            detail.rating = destination.getRating();
            detail.categories = destination.getCategories().stream()
                    .map(CategoryDetail::from)
                    .toList();
            detail.latitude = destination.getLatitude();
            detail.longtitude = destination.getLongtitude();
            detail.thumbnailUrl = destination.getThumbnailUrl();
            return detail;
        }
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
