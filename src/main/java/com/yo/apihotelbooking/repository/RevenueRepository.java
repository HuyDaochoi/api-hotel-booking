package com.yo.apihotelbooking.repository;

import com.yo.apihotelbooking.schemas.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface RevenueRepository extends JpaRepository<Booking, Long> { 
    
    @Query(value = "SELECT * FROM v_revenue_by_month", nativeQuery = true)
    List<Object[]> getMonthlyRevenueNative();
}