package com.logistics.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "logistics_load")
public class Load {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private String weight;
    private String pickupLocation;
    private String deliveryLocation;
    private String status; // e.g., "Pending", "In Transit", "Delivered"


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
