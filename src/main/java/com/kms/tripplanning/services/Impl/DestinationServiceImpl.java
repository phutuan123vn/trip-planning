package com.kms.tripplanning.services.Impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.kms.tripplanning.dto.destination.DestinationMapper;
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

    private final DestinationMapper destinationMapper;

    @Override
    public Page<DestinationDetail> listDestination(Map<String, List<String>> filters, String sortBy,
            String sortDirection, int page, int size) {

        FilterBuilderHelper filterBuilder = new FilterBuilderHelper();
        filters.forEach(filterBuilder::addFilter);
        var sort = FilterBuilderHelper.buildSort(sortBy, sortDirection);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Destination> destinations = destinationRepository.search(filterBuilder.build(), pageRequest,
                List.of("categories"));
        return destinationRepository.castDTO(destinations, destinationMapper::toDestinationDetail);
    }

    @Override
    public DestinationDetail getDestinationById(UUID destinationId) {
        var destination = destinationRepository.findById(destinationId)
                .orElseThrow(
                        () -> new NotFoundException("Destionation not Found with id: " + destinationId.toString()));
        return destinationMapper.toDestinationDetail(destination);
    }

    @Override
    public DestinationDetail createDestination(DestinationCreate request) {
        Destination destination = destinationMapper.toDestination(request);
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

            if (categories.size() != request.getCategoryIds().size()) {
                throw new NotFoundException("One or more categories not found with provided ids");
            }
            destination.setCategories(Set.copyOf(categories));
        }
        destinationRepository.save(destination);
        return destinationMapper.toDestinationDetail(destination);
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
        destinationMapper.updateDestination(request, destination);
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

            if (categories.size() != request.getCategoryIds().size()) {
                throw new NotFoundException("One or more categories not found with provided ids");
            }
            destination.setCategories(Set.copyOf(categories));
        }
        destinationRepository.save(destination);
        return destinationMapper.toDestinationDetail(destination);
    }

}
