package com.yo.apihotelbooking.schemas.domain;
import com.yo.apihotelbooking.schemas.AuditableEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;

@Entity

@Getter @Setter 
@Data
@Table(name = "room_images")
public class RoomImages extends AuditableEntity {

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    private String caption;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
