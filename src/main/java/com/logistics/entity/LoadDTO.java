package com.logistics.entity;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LoadDTO {
    private Long id;
    private Long customerId;
    private String description;
    private String weight;
    private BigDecimal actualWeight;
    private String pickupLocation;
    private String deliveryLocation;
    private String status;
    private CargoType cargoType;
    private boolean cargoTypeDefaulted;
    private PricingMode pricingMode;
    private BigDecimal ratePerKm;
    private BigDecimal flatAmount;
    private Long assignedDriverId;
    private String customerName; // Optional: include customer name if needed

    // Constructors, getters, and setters

    public LoadDTO() {
    }

    public LoadDTO(Long id, Long customerId, String description, String weight, String pickupLocation, String deliveryLocation, String status, String customerName) {
        this.id = id;
        this.customerId = customerId;
        this.description = description;
        this.weight = weight;
        this.pickupLocation = pickupLocation;
        this.deliveryLocation = deliveryLocation;
        this.status = status;
        this.customerName = customerName;
    }
}