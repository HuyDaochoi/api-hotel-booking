package com.yo.apihotelbooking.schemas.domain;

import java.math.BigDecimal;

import com.yo.apihotelbooking.schemas.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import com.yo.apihotelbooking.schemas.domain.RoomImage;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_types")
@Getter
@Setter
@Data
public class RoomType extends AuditableEntity {

    @Column(columnDefinition = "varchar(100)", nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "text")  
    private String description;

     @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "cancellation_policy")
    private String cancellationPolicy;

    @Column(name = "is_active")
    private Boolean isActive = true;

   @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC") // Tự động sắp xếp ảnh theo thứ tự khi query
    private List<RoomImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RoomAmenity> amenities = new ArrayList<>();
}
