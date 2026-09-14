package com.logistics.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "logistics_load")
public class Load {
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_IN_TRANSIT = "In Transit";
    public static final String STATUS_DELIVERED = "Delivered";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private String weight;
    private String pickupLocation;
    private String deliveryLocation;
    private String status = STATUS_PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo_type")
    private CargoType cargoType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_mode")
    private PricingMode pricingMode;

    @Column(name = "rate_per_km", precision = 19, scale = 4)
    private BigDecimal ratePerKm;

    @Column(name = "flat_amount", precision = 19, scale = 2)
    private BigDecimal flatAmount;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "vehicle", "faults", "password"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_driver_id")
    private Driver assignedDriver;

    // Ensure you have getters and setters for customerId
    // Add this field explicitly
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "loads"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @JsonIgnore
    @OneToMany(mappedBy = "load")
    private List<Trip> trips;

    // Getters, setters, constructors
}
