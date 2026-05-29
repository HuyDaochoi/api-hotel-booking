package com.yo.apihotelbooking.repository;

import com.yo.apihotelbooking.schemas.domain.Amenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenitiesRepository extends JpaRepository<Amenities, Long> {
}