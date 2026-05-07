package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    List<PricingRule> findByIsActiveTrue();

    @Query("""
        SELECT p FROM PricingRule p
        WHERE p.isActive = true
        AND (p.roomType.id = :roomTypeId OR p.roomType IS NULL)
        ORDER BY p.priority DESC
    """)
    List<PricingRule> findApplicableRules(Long roomTypeId);

}
