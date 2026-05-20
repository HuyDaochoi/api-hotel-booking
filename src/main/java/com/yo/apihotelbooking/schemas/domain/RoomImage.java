package com.yo.apihotelbooking.schemas.domain;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "room_images")
@Data
public class RoomImage {
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    private String caption;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false) 
    @JsonBackReference
    private RoomType roomType; 
}