package com.yo.apihotelbooking.schemas.domain;

import com.yo.apihotelbooking.schemas.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "cancellation_policies")
@Getter
@Setter
public class CancellationPolicy extends AuditableEntity {
    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;
    @Column(name = "is_force_majeure")
    private Boolean isForceMajeure = false;
    private String policyName;
    private Integer hoursBeforeCheckin;
    private BigDecimal refundPercent;
    public Boolean getIsForceMajeure() {
        return isForceMajeure != null ? isForceMajeure : false;
    }
    private String description;
}