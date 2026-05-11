package com.kms.tripplanning.services.Impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.kms.tripplanning.dto.destination.RequestDestination.DestinationCreate;
import com.kms.tripplanning.dto.destination.RequestDestination.DestinationUpdate;
import com.kms.tripplanning.dto.destination.ResponseDestination.DestinationDetail;
import com.kms.tripplanning.entity.Category;
import com.kms.tripplanning.entity.Destination;
import com.kms.tripplanning.exception.types.NotFoundException;
import com.kms.tripplanning.repository.CategoryRepository;
import com.kms.tripplanning.repository.DestinationRepository;
import com.kms.tripplanning.services.DestinationService;
import com.kms.tripplanning.utils.FilterBuilderHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DestinationServiceImpl implements DestinationService {

    private final DestinationRepository destinationRepository;

    private final CategoryRepository categoryRepository;

    @Override
    public Page<DestinationDetail> listDestination(Map<String, List<String>> filters, String sortBy,
            String sortDirection, int page, int size) {

        FilterBuilderHelper filterBuilder = new FilterBuilderHelper();
        filters.forEach(filterBuilder::addFilter);
        var sort = FilterBuilderHelper.buildSort(sortBy, sortDirection);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Destination> destinations = destinationRepository.search(filterBuilder.build(), pageRequest,
                List.of("categories"));
        return destinationRepository.castDTO(destinations, DestinationDetail::from);
    }

    @Override
    public DestinationDetail getDestinationById(UUID destinationId) {
        var destination = destinationRepository.findById(destinationId)
                .orElseThrow(
                        () -> new NotFoundException("Destionation not Found with id: " + destinationId.toString()));
        return DestinationDetail.from(destination);
    }

    @Override
    public DestinationDetail createDestination(DestinationCreate request) {
        Destination destination = Destination.builder()
                .name(request.getName())
                .city(request.getCity())
                .country(request.getCountry())
                .rating(request.getRating())
                .latitude(request.getLatitude())
                .longtitude(request.getLongitude())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

            if (categories.size() != request.getCategoryIds().size()) {
                throw new NotFoundException("One or more categories not found with provided ids");
            }
            destination.setCategories(Set.copyOf(categories));
        }
        destinationRepository.save(destination);
        return DestinationDetail.from(destination);
    }

    @Override
    public void deleteDestination(UUID destinationId) {
        var destination = destinationRepository.findById(destinationId)
                .orElseThrow(
                        () -> new NotFoundException("Destionation not Found with id: " + destinationId.toString()));
        destinationRepository.delete(destination);
    }

    @Override
    public DestinationDetail updateDestination(UUID destinationId, DestinationUpdate request) {
        var destination = destinationRepository.findById(destinationId)
                .orElseThrow(
                        () -> new NotFoundException("Destionation not Found with id: " + destinationId.toString()));
        destination.setName(request.getName() != null ? request.getName() : destination.getName());
        destination.setCity(request.getCity() != null ? request.getCity() : destination.getCity());
        destination.setCountry(request.getCountry() != null ? request.getCountry() : destination.getCountry());
        destination.setRating(request.getRating() != null ? request.getRating() : destination.getRating());
        destination.setLatitude(request.getLatitude() != null ? request.getLatitude() : destination.getLatitude());
        destination
                .setLongtitude(request.getLongitude() != null ? request.getLongitude() : destination.getLongtitude());
        destination.setThumbnailUrl(
                request.getThumbnailUrl() != null ? request.getThumbnailUrl() : destination.getThumbnailUrl());
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

            if (categories.size() != request.getCategoryIds().size()) {
                throw new NotFoundException("One or more categories not found with provided ids");
            }
            destination.setCategories(Set.copyOf(categories));
        }
        destinationRepository.save(destination);
        return DestinationDetail.from(destination);
    }

}
