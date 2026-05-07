package com.yo.apihotelbooking.schemas.domain;
import com.yo.apihotelbooking.schemas.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
@Table(name = "rooms")
@Entity
@Data
public class Room extends AuditableEntity {
   

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

     @Column(name = "room_number", nullable = false, unique = true)
    private String roomNumber;
    @Column(name = "floor")
    private Integer floor;

    @Column(columnDefinition = "text")
    private String description;
 @Column(name = "is_active")
    private Boolean isActive = true;

   
  
}
