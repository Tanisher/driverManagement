package com.logistics.DTO;

import com.logistics.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerDTO toDTO(Customer customer) {
        if (customer == null) {
            return null;
        }
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setContact(customer.getContact());
        dto.setAddress(customer.getAddress());
        dto.setNumberOfLoads(customer.getNumberOfLoads());
        dto.setEmail(customer.getEmail());
        dto.setAccountNumber(customer.getAccountNumber());
        dto.setPaymentTerms(customer.getPaymentTerms());
        dto.setBillingAddress(customer.getBillingAddress());
        return dto;
    }

    public Customer toEntity(CustomerDTO dto) {
        if (dto == null) {
            return null;
        }
        Customer customer = new Customer();
        apply(dto, customer);
        return customer;
    }

    public void apply(CustomerDTO dto, Customer customer) {
        customer.setName(dto.getName());
        customer.setContact(dto.getContact());
        customer.setAddress(dto.getAddress());
        customer.setNumberOfLoads(dto.getNumberOfLoads());
        customer.setEmail(dto.getEmail());
        customer.setAccountNumber(dto.getAccountNumber());
        customer.setPaymentTerms(dto.getPaymentTerms());
        customer.setBillingAddress(dto.getBillingAddress());
    }

    public void applyNonNull(CustomerDTO dto, Customer customer) {
        if (dto.getName() != null) {
            customer.setName(dto.getName());
        }
        if (dto.getContact() != null) {
            customer.setContact(dto.getContact());
        }
        if (dto.getAddress() != null) {
            customer.setAddress(dto.getAddress());
        }
        if (dto.getNumberOfLoads() != null) {
            customer.setNumberOfLoads(dto.getNumberOfLoads());
        }
        if (dto.getEmail() != null) {
            customer.setEmail(dto.getEmail());
        }
        if (dto.getAccountNumber() != null) {
            customer.setAccountNumber(dto.getAccountNumber());
        }
        if (dto.getPaymentTerms() != null) {
            customer.setPaymentTerms(dto.getPaymentTerms());
        }
        if (dto.getBillingAddress() != null) {
            customer.setBillingAddress(dto.getBillingAddress());
        }
    }
}
