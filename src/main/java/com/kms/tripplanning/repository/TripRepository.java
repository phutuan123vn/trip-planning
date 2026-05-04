package com.kms.tripplanning.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.kms.tripplanning.entity.Trip;
import com.kms.tripplanning.utils.BaseRepository;

@Repository
public interface TripRepository extends BaseRepository<Trip, UUID> {    
}
