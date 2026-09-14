package com.logistics.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndDeadheadRequest {

    @NotNull(message = "endMileage is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "endMileage must not be negative")
    private Double endMileage;
}
