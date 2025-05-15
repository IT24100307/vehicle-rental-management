package com.system.project1.service;

import com.system.project1.entity.Customer;
import com.system.project1.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    // Create
    public Customer createCustomer(Customer customer) {

        return customerRepository.save(customer);
    }

    // Read one
    public Optional<Customer> getCustomerById(String id) {

        return customerRepository.findById(id);
    }

    // Read all
    public List<Customer> getAllCustomers() {

        return customerRepository.findAll();
    }

    // Update
    public Customer updateCustomer(Customer customer) {

        return customerRepository.save(customer);
    }

    // Delete
    public void deleteCustomer(String id) {

        customerRepository.deleteById(id);
    }

    // Check if customer exists
    public boolean existsById(String id) {

        return customerRepository.existsById(id);
    }
}
