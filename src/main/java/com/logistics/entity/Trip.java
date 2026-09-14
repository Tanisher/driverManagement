package com.logistics.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Shared shape for both legs of a load: deadhead (empty) then loaded (cargo onboard).
 * Existing DriverTrip fields are preserved here and mapped onto the leg they belong to.
 */
@Getter
@Setter
@Entity
@Table(name = "trip")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "leg_type", discriminatorType = DiscriminatorType.STRING)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "legType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeadheadTrip.class, name = "DEADHEAD"),
        @JsonSubTypes.Type(value = LoadedTrip.class, name = "LOADED")
})
public abstract class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Carried over from DriverTrip.dateTime — same instant as {@link #startTime}.
     */
    private LocalDateTime dateTime;

    /**
     * Carried over from DriverTrip.destination.
     * Deadhead: load pickup. Loaded: load delivery.
     */
    private String destination;

    @JsonAlias({"StartingMillage", "startingMillage"})
    @Column(name = "start_mileage", nullable = false)
    private Double startMileage;

    @JsonAlias({"EndingMillage", "endingMillage"})
    @Column(name = "end_mileage")
    private Double endMileage;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status;

    @Column(name = "trip_group_id", nullable = false, length = 36)
    private String tripGroupId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(optional = false)
    @JoinColumn(name = "load_id", nullable = false)
    private Load load;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    /**
     * Carried over from DriverTrip.plateNumber — snapshot of the assigned vehicle plate.
     */
    private String plateNumber;

    /**
     * Carried over from DriverTrip.customer — the load's customer.
     */
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Transient
    @JsonProperty("legType")
    public abstract TripLegType getLegType();
}
