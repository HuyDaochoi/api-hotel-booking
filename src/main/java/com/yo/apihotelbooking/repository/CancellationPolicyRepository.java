package com.yo.apihotelbooking.repository;

import com.yo.apihotelbooking.schemas.domain.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, Long> {

    @Query("SELECT c FROM CancellationPolicy c WHERE c.roomType.id = :roomTypeId")
    List<CancellationPolicy> findByRoomTypeId(@Param("roomTypeId") Long roomTypeId);

    @Query("SELECT c FROM CancellationPolicy c WHERE c.roomType IS NULL")
    List<CancellationPolicy> findDefaultPolicies();
}