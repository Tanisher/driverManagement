package com.logistics.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ActualWeightRequest {

    @NotNull(message = "actualWeight is required")
    @Positive(message = "actualWeight must be greater than 0")
    private BigDecimal actualWeight;
}
