package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    
    @Query("SELECT p FROM PricingRule p WHERE p.isActive = true " +
           "AND (p.roomType.id = :roomTypeId OR p.roomType IS NULL) " +
           "AND (" +
           "  (p.ruleType = 'SEASONAL' AND :date BETWEEN p.startDate AND p.endDate) OR " +
           "  (p.ruleType = 'WEEKEND' AND (:dayOfWeek = 5 OR :dayOfWeek = 6 OR :dayOfWeek = 7)) OR " +
           "  (p.ruleType = 'SPECIAL_EVENT' AND :date BETWEEN p.startDate AND p.endDate) " +
           ")")
    List<PricingRule> findActiveRulesForDate(@Param("date") LocalDate date, 
                                            @Param("dayOfWeek") int dayOfWeek,
                                             @Param("roomTypeId") Long roomTypeId);
}