package com.logistics.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Unloaded travel from the driver's location to the load's pickup point.
 */
@Getter
@Setter
@Entity
@Table(name = "deadhead_trip")
@DiscriminatorValue("DEADHEAD")
public class DeadheadTrip extends Trip {

    @JsonIgnore
    @OneToOne(mappedBy = "deadheadTrip")
    private LoadedTrip loadedTrip;

    @Override
    public TripLegType getLegType() {
        return TripLegType.DEADHEAD;
    }
}
