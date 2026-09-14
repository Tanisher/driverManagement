package com.logistics.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndDeadheadRequest {

    @NotNull(message = "endMileage is required")
    private Double endMileage;
}
