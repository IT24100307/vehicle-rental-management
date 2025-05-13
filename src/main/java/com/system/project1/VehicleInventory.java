package com.system.project1;

import com.system.project1.entity.*;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

@Service
public class VehicleInventory {
    private Vehicle[] vehicles;
    private int vehicleCount;
    private static final int MAX_VEHICLES = 20;
    private static final String IMAGE_DIR = "src/main/resources/static/images/";

    public VehicleInventory() {
        vehicles = new Vehicle[MAX_VEHICLES];
        vehicleCount = 0;
        new File(IMAGE_DIR).mkdirs();
    }

    public void addVehicle(Vehicle vehicle) {
        if (vehicleCount < MAX_VEHICLES && vehicle != null) {
            vehicles[vehicleCount] = vehicle;
            vehicleCount++;
            sortVehiclesByType(); // Sort vehicles after adding a new one
        }
    }

    public boolean deleteVehicle(String vehicleID) {
        for (int i = 0; i < vehicleCount; i++) {
            if (vehicles[i] != null && vehicles[i].getVehicleID().equals(vehicleID)) {
                for (int j = i; j < vehicleCount - 1; j++) {
                    vehicles[j] = vehicles[j + 1];
                }
                vehicles[vehicleCount - 1] = null;
                vehicleCount--;
                return true;
            }
        }
        return false;
    }

    public boolean updateVehicle(String vehicleID, Vehicle updatedVehicle) {
        for (int i = 0; i < vehicleCount; i++) {
            if (vehicles[i] != null && vehicles[i].getVehicleID().equals(vehicleID)) {
                vehicles[i].setBrand(updatedVehicle.getBrand());
                vehicles[i].setModel(updatedVehicle.getModel());
                vehicles[i].setRentPrice(updatedVehicle.getRentPrice());

                // Only update image path if it's provided
                if (updatedVehicle.getImagePath() != null) {
                    vehicles[i].setImagePath(updatedVehicle.getImagePath());
                }

                if (vehicles[i] instanceof Car && updatedVehicle instanceof Car) {
                    ((Car) vehicles[i]).setNumSeats(((Car) updatedVehicle).getNumSeats());
                    ((Car) vehicles[i]).setHasAC(((Car) updatedVehicle).getHasAC());
                    ((Car) vehicles[i]).setTransmissionType(((Car) updatedVehicle).getTransmissionType());
                } else if (vehicles[i] instanceof Bike && updatedVehicle instanceof Bike) {
                    ((Bike) vehicles[i]).setBikeType(((Bike) updatedVehicle).getBikeType());
                    ((Bike) vehicles[i]).setEngineCapacity(((Bike) updatedVehicle).getEngineCapacity());
                } else if (vehicles[i] instanceof Van && updatedVehicle instanceof Van) {
                    ((Van) vehicles[i]).setNumberOfSeat(((Van) updatedVehicle).getNumberOfSeat());
                    ((Van) vehicles[i]).setHasSlidingDoors(((Van) updatedVehicle).getHasSlidingDoors());
                } else if (vehicles[i] instanceof Lorry && updatedVehicle instanceof Lorry) {
                    ((Lorry) vehicles[i]).setMaxLoadCapacity(((Lorry) updatedVehicle).getMaxLoadCapacity());
                    ((Lorry) vehicles[i]).setNumAxles(((Lorry) updatedVehicle).getNumAxles());
                } else if (vehicles[i] instanceof Bus && updatedVehicle instanceof Bus) {
                    ((Bus) vehicles[i]).setPassengerCapacity(((Bus) updatedVehicle).getPassengerCapacity());
                    ((Bus) vehicles[i]).setHasWheelchairAccess(((Bus) updatedVehicle).getHasWheelchairAccess());
                }
                sortVehiclesByType(); // Resort after updating
                return true;
            }
        }
        return false;
    }

    public Vehicle[] getVehicles() {
        return vehicles;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }

    // Helper method to get the type priority for sorting
    private int getVehicleTypePriority(Vehicle vehicle) {
        if (vehicle instanceof Car)
            return 1;
        if (vehicle instanceof Bike)
            return 2;
        if (vehicle instanceof Van)
            return 3;
        if (vehicle instanceof Lorry)
            return 4;
        if (vehicle instanceof Bus)
            return 5;
        return 6; // Unknown type
    }

    // Sort vehicles by their type
    private void sortVehiclesByType() {
        Arrays.sort(vehicles, 0, vehicleCount, new Comparator<Vehicle>() {
            @Override
            public int compare(Vehicle v1, Vehicle v2) {
                if (v1 == null && v2 == null)
                    return 0;
                if (v1 == null)
                    return 1;
                if (v2 == null)
                    return -1;

                return Integer.compare(getVehicleTypePriority(v1), getVehicleTypePriority(v2));
            }
        });
    }

    // Filter vehicles by type
    public Vehicle[] filterVehiclesByType(int typeChoice) {
        // Return early if we want all vehicles
        if (typeChoice == 0) {
            // Create a copy of non-null vehicles
            int nonNullCount = 0;
            for (int i = 0; i < vehicleCount; i++) {
                if (vehicles[i] != null)
                    nonNullCount++;
            }

            Vehicle[] result = new Vehicle[nonNullCount];
            int index = 0;
            for (int i = 0; i < vehicleCount; i++) {
                if (vehicles[i] != null) {
                    result[index++] = vehicles[i];
                }
            }
            return result;
        }

        // Filter by specific type
        int filteredCount = 0;
        for (int i = 0; i < vehicleCount; i++) {
            if (vehicles[i] != null) {
                boolean match = false;
                switch (typeChoice) {
                    case 1:
                        match = (vehicles[i] instanceof Car);
                        break;
                    case 2:
                        match = (vehicles[i] instanceof Bike);
                        break;
                    case 3:
                        match = (vehicles[i] instanceof Van);
                        break;
                    case 4:
                        match = (vehicles[i] instanceof Lorry);
                        break;
                    case 5:
                        match = (vehicles[i] instanceof Bus);
                        break;
                }
                if (match) {
                    filteredCount++;
                }
            }
        }

        Vehicle[] filtered = new Vehicle[filteredCount];
        int index = 0;
        for (int i = 0; i < vehicleCount; i++) {
            if (vehicles[i] != null) {
                boolean match = false;
                switch (typeChoice) {
                    case 1:
                        match = (vehicles[i] instanceof Car);
                        break;
                    case 2:
                        match = (vehicles[i] instanceof Bike);
                        break;
                    case 3:
                        match = (vehicles[i] instanceof Van);
                        break;
                    case 4:
                        match = (vehicles[i] instanceof Lorry);
                        break;
                    case 5:
                        match = (vehicles[i] instanceof Bus);
                        break;
                }
                if (match) {
                    filtered[index++] = vehicles[i];
                }
            }
        }

        return filtered;
    }

    // Get the vehicle type as an integer (1-5)
    public int getVehicleTypeAsInt(Vehicle vehicle) {
        if (vehicle instanceof Car)
            return 1;
        if (vehicle instanceof Bike)
            return 2;
        if (vehicle instanceof Van)
            return 3;
        if (vehicle instanceof Lorry)
            return 4;
        if (vehicle instanceof Bus)
            return 5;
        return 0; // Unknown type
    }
}