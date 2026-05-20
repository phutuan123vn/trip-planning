package com.kms.tripplanning.dto.trip;

import java.time.ZoneOffset;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.kms.tripplanning.dto.trip.ResponseTrip.TripDetail;
import com.kms.tripplanning.entity.Trip;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, imports = { ZoneOffset.class })
public interface TripMapper {

    @Mapping(target = "startDate", expression = "java(trip.getStartDate() != null ? trip.getStartDate().toString() : null)")
    @Mapping(target = "endDate", expression = "java(trip.getEndDate() != null ? trip.getEndDate().toString() : null)")
    TripDetail toTripDetail(Trip trip);

    @Mapping(target = "startDate", expression = "java(requestTrip.getStartDate() != null ? requestTrip.getStartDate().withOffsetSameInstant(ZoneOffset.UTC) : null)")
    @Mapping(target = "endDate", expression = "java(requestTrip.getEndDate() != null ? requestTrip.getEndDate().withOffsetSameInstant(ZoneOffset.UTC) : null)")
    Trip toTrip(RequestTrip.TripCreate requestTrip);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "startDate", expression = "java(requestTrip.getStartDate() != null ? requestTrip.getStartDate().withOffsetSameInstant(ZoneOffset.UTC) : trip.getStartDate())")
    @Mapping(target = "endDate", expression = "java(requestTrip.getEndDate() != null ? requestTrip.getEndDate().withOffsetSameInstant(ZoneOffset.UTC) : trip.getEndDate())")
    @Mapping(target = "destinations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateTrip(RequestTrip.TripUpdate requestTrip, @MappingTarget Trip trip);

}
