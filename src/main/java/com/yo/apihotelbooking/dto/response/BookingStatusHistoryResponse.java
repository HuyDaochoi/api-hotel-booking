package com.yo.apihotelbooking.dto.response;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data

public class BookingStatusHistoryResponse {

    private BookingStatus oldStatus;
    private BookingStatus newStatus;

    private String changedBy;
    private String note;

    private LocalDateTime changedAt;
}