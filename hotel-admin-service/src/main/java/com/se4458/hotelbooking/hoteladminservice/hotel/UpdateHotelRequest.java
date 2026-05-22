package com.se4458.hotelbooking.hoteladminservice.hotel;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record UpdateHotelRequest(
        @NotBlank String name,
        String description,
        @NotBlank String destination,
        @NotBlank String address,
        @NotNull BigDecimal latitude,
        @NotNull BigDecimal longitude,
        @NotNull @DecimalMin("1.0") @DecimalMax("5.0") BigDecimal starRating,
        List<String> amenities
) {
}
