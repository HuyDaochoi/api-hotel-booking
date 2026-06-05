package com.yo.apihotelbooking.schemas.domain;

import java.math.BigDecimal;

import com.yo.apihotelbooking.schemas.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import com.yo.apihotelbooking.schemas.domain.RoomImage;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Fetch;
@Entity
@Table(name = "room_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"images", "amenities"}) 
@EqualsAndHashCode(callSuper = true, exclude = {"images", "amenities"})
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
    @OrderBy("sortOrder ASC") 
    @Fetch(FetchMode.SUBSELECT)
    private List<RoomImage> images = new ArrayList<>();

@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "room_type_amenities", 
    joinColumns = @JoinColumn(name = "room_type_id"),
    inverseJoinColumns = @JoinColumn(name = "amenity_id")
)
private List<Amenities> amenities = new ArrayList<>();
}
