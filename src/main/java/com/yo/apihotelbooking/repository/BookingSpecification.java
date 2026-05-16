package com.yo.apihotelbooking.repository;

import com.yo.apihotelbooking.schemas.domain.Booking;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingSpecification {

    private BookingSpecification() {}

    public static Specification<Booking> filterBy(
            BookingStatus status,
            LocalDate checkIn,
            LocalDate checkOut,
            Long roomId,
            Long userId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (checkIn != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInDate"), checkIn));
            }
            if (checkOut != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkOutDate"), checkOut));
            }
            if (roomId != null) {
                predicates.add(cb.equal(root.get("room").get("id"), roomId));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}