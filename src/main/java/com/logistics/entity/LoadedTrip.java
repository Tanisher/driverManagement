package com.logistics.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Cargo-onboard leg: pickup to delivery. Same shape as deadhead, plus fuel and trailers.
 */
@Getter
@Setter
@Entity
@Table(name = "loaded_trip")
@DiscriminatorValue("LOADED")
public class LoadedTrip extends Trip {

    @JsonAlias({"FuelLitres", "fuelLitres"})
    @Column(name = "fuel_litres")
    private Double fuelLitres;

    private String trailer1;

    private String trailer2;

    @OneToOne
    @JoinColumn(name = "deadhead_trip_id")
    private DeadheadTrip deadheadTrip;

    @Override
    public TripLegType getLegType() {
        return TripLegType.LOADED;
    }
}
