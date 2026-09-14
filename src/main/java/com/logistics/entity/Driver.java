

package com.logistics.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Driver extends User {

    private String name;
    private String lastName;
    private String address;
    private String licenseNumber;
    private String nextOfKin;
    private String nextOfKinContact;
    private String mobileNumber;
    private String idNumber;
    private String nationalId;
    private LocalDate licenseExpiryDate;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = true)
    private Vehicle vehicle;

    @JsonIgnore
    @OneToMany(mappedBy = "driver")
    private List<Fault> faults;

    public Driver() {
        setRole(UserRole.DRIVER);
    }
}
