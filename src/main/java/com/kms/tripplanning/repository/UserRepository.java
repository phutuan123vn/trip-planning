package com.kms.tripplanning.repository;

import org.springframework.stereotype.Repository;

import com.kms.tripplanning.entity.User;
import com.kms.tripplanning.utils.BaseRepository;

@Repository
public interface UserRepository extends BaseRepository<User, String> {
    
}
