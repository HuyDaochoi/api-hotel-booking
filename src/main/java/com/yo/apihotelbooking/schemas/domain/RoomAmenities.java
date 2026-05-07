package com.yo.apihotelbooking.schemas.domain;
import com.yo.apihotelbooking.schemas.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "room_amenities")
@Getter @Setter
public class RoomAmenities extends AuditableEntity {

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "amenity_name", nullable = false,columnDefinition = "Varchar(100)")
    private String amenityName;

    @Column(name = "icon_code",columnDefinition = "Varchar(50)")
    private String iconCode;
}