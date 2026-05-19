package com.yo.apihotelbooking.schemas.domain;
import com.yo.apihotelbooking.schemas.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "room_amenities")
@Getter
@Setter
public class RoomAmenity  extends AuditableEntity{
   

    @Column(name = "amenity_name", nullable = false)
    private String amenityName;

    @Column(name = "icon_code")
    private String iconCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false) 
    private RoomType roomType;
}