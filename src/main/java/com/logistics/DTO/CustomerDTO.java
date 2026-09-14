package com.logistics.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    private String name;
    private String contact;
    private String address;
    private String numberOfLoads;
    private String email;
    private String accountNumber;
    private String paymentTerms;
    private String billingAddress;
}
