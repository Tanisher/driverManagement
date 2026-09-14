package com.logistics.DTO;

import com.logistics.entity.PricingMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadBillableAmountResponse {
    private Long loadId;
    private PricingMode pricingMode;
    private BigDecimal billableAmount;
    private BigDecimal loadedKm;
}
