package com.kms.tripplanning.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.kms.tripplanning.entity.Destination;
import com.kms.tripplanning.utils.BaseRepository;

@Repository
public interface DestinationRepository extends BaseRepository<Destination, UUID> {

}
