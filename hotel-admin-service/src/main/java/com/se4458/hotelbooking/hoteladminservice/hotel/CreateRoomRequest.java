package com.se4458.hotelbooking.hoteladminservice.hotel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateRoomRequest(
        @NotBlank String roomType,
        @Min(1) int capacity,
        @Min(1) int totalCount,
        @NotNull @DecimalMin("0.01") BigDecimal pricePerNight
) {
}
