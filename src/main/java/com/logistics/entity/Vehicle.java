/**
 * File Name: Vehicle.java
 * Last Modified By: Kudzie
 * Last Modified On: 2024/12/22
 * Description: vehicle tracking up to date
 */

package com.logistics.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "vehicles")

public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String licensePlate;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(name = "model_year", nullable = false)
    private int year;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private boolean active;

    @Column
    private LocalDate lastServiceDate;

    @JsonIgnore
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Fault> faults;

    // Location fields
    @Column(nullable = true)
    private Double latitude;

    @Column(nullable = true)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private CargoType vehicleType;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    public void setDriver(Driver driver) {
        if (this.driver != null) {
            this.driver.setVehicle(null);
        }
        this.driver = driver;
        if (driver != null) {
            driver.setVehicle(this);
        }
    }
}


