package com.logistics.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter

public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String contact;
    private String address;
    private String numberOfLoads;
    private String email;

    // Relationships

    @JsonIgnore
    @OneToMany(mappedBy = "customer")
    private List<Load> loads = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "customer")
    private List<Trip> driverTrips;

}

