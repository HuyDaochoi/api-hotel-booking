package com.yo.apihotelbooking.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import com.yo.apihotelbooking.dto.request.CreateRoomAmenityRequest;
import com.yo.apihotelbooking.dto.request.CreateRoomImageRequest;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateRoomTypeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;
    private List<Long> amenityIds;
    private List<CreateRoomImageRequest> images;
    @Size(max = 1000, message = "Description too long")
    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be > 0")
    private BigDecimal basePrice;

    @NotNull(message = "Max capacity is required")
    @Min(value = 1, message = "Max capacity must be at least 1")
    @Max(value = 20, message = "Max capacity too large")
    private Integer maxCapacity;

    @Size(max = 2000, message = "Cancellation policy too long")
    private String cancellationPolicy;
}