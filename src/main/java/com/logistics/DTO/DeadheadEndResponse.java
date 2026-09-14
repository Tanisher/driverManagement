package com.logistics.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeadheadEndResponse {

    private TripResponse deadheadTrip;
    private TripResponse loadedTrip;
}
