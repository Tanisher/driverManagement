package com.logistics.DTO;

import com.logistics.entity.CargoType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {

    private Long id;
    private String licensePlate;
    private String make;
    private String model;
    private int year;  // changed from Integer to int
    private String color;
    private boolean active;  // changed from Boolean to boolean
    private LocalDate lastServiceDate;  // changed from LocalDateTime to LocalDate
    private CargoType vehicleType;
    private Double latitude;
    private Double longitude;
    private DriverDTO driver;
    private List<FaultDTO> faults;
}
