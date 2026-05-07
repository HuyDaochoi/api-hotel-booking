package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    List<RoomType> findByIsActiveTrue();

}