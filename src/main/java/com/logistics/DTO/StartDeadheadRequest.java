package com.logistics.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartDeadheadRequest {

    @NotNull(message = "loadId is required")
    private Long loadId;

    @NotNull(message = "startMileage is required")
    private Double startMileage;
}
