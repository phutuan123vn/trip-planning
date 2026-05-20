package com.kms.tripplanning.dto.destination;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.kms.tripplanning.dto.destination.ResponseDestination.DestinationDetail;
import com.kms.tripplanning.dto.destination.ResponseDestination.DestinationDropdown;
import com.kms.tripplanning.entity.Destination;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DestinationMapper {

    DestinationDetail toDestinationDetail(Destination destination);

    DestinationDropdown toDestinationDropdown(Destination destination);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "id", ignore = true)
    Destination toDestination(RequestDestination.DestinationCreate requestDestinationCreate);
    

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateDestination(RequestDestination.DestinationUpdate requestDestinationUpdate, @MappingTarget Destination destination);

}
