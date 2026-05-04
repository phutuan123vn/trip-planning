package com.kms.tripplanning.repository;

import java.util.UUID;
import java.util.Locale.Category;

import org.springframework.stereotype.Repository;

import com.kms.tripplanning.utils.BaseRepository;

@Repository
public interface CategoryRepository extends BaseRepository<Category, UUID> {

}
