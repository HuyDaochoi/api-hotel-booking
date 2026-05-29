package com.yo.apihotelbooking.repository;

import com.yo.apihotelbooking.schemas.domain.BookingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface BookingRuleRepository extends JpaRepository<BookingRule, Long> {


    Optional<BookingRule> findFirstByRoomTypeId(Long roomTypeId);


    @Query("SELECT r FROM BookingRule r WHERE r.isActive = true ORDER BY r.id ASC")
    List<BookingRule> findAllActiveRules();


    default BookingRule findDefaultRule() {
        List<BookingRule> rules = findAllActiveRules();
        return rules.isEmpty() ? null : rules.get(0);
    }
}