package com.logistics.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndLoadedTripRequest {

    @NotNull(message = "endMileage is required")
    private Double endMileage;

    private Double fuelLitres;

    private String trailer1;

    private String trailer2;
}
