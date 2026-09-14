package com.logistics.util;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverAssignmentRequest {
    @NotNull(message = "driverId is required")
    private Long driverId;
}
