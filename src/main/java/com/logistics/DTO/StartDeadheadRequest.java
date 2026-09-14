package com.logistics.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartDeadheadRequest {

    @NotNull(message = "loadId is required")
    private Long loadId;

    @NotNull(message = "startMileage is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "startMileage must not be negative")
    private Double startMileage;
}
