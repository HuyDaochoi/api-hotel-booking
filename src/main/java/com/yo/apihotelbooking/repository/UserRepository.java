package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface UserRepository extends JpaRepository<User, Long> {

Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
@Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
           "(:role IS NULL OR r.name = :role) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive) AND " +
           "(:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> findAllWithFilter(
        @Param("role") String role, 
        @Param("isActive") Boolean isActive, 
        @Param("keyword") String keyword, 
        Pageable pageable
    );
}