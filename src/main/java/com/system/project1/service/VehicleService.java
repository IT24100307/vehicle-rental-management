package com.system.project1.service;

import com.system.project1.entity.Vehicle;
import com.system.project1.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;

    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public Vehicle findVehicleById(String id) {
        return vehicleRepository.findById(id).orElse(null);
    }
}