package com.system.project1.util;

import com.system.project1.entity.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.springframework.stereotype.Component;

@Component
public class FileStorageManager {
    private static final String DATA_DIR = "data";
    private static final String CUSTOMERS_FILE = DATA_DIR + "/customers.txt";
    private static final String VEHICLES_FILE = DATA_DIR + "/vehicles.txt";
    private static final String EVENTS_FILE = DATA_DIR + "/events.txt";
    private static final String DISCOUNTS_FILE = DATA_DIR + "/discounts.txt";
    private static final String STAFF_FILE = DATA_DIR + "/staff.txt";
    private static final String EVENT_BOOKINGS_FILE = DATA_DIR + "/event_bookings.txt";
    private static final String PURCHASED_VEHICLES_FILE = DATA_DIR + "/purchased_vehicles.txt";
    private static final String PAYMENTS_FILE = DATA_DIR + "/payments.txt";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public FileStorageManager() {
        // Create data directory if it doesn't exist
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Create files if they don't exist
        createFileIfNotExists(CUSTOMERS_FILE);
        createFileIfNotExists(VEHICLES_FILE);
        createFileIfNotExists(EVENTS_FILE);
        createFileIfNotExists(DISCOUNTS_FILE);
        createFileIfNotExists(STAFF_FILE);
        createFileIfNotExists(EVENT_BOOKINGS_FILE);
        createFileIfNotExists(PURCHASED_VEHICLES_FILE);
        createFileIfNotExists(PAYMENTS_FILE);
    }

    private void createFileIfNotExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating file: " + filePath);
                e.printStackTrace();
            }
        }
    }

    // Customer methods
    public List<Customer> loadAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CUSTOMERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    Customer customer = new Customer();
                    customer.setCustomerId(Integer.parseInt(parts[0]));
                    customer.setName(parts[1]);
                    customer.setContactNumber(parts[2]);
                    customer.setDriverLicenseNumber(Integer.parseInt(parts[3]));
                    customer.setEmail(parts[4]);
                    customer.setPassword(parts[5]);
                    if (parts.length > 6) {
                        customer.setAddress(parts[6]);
                    }
                    customers.add(customer);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading customers from file");
            e.printStackTrace();
        }
        return customers;
    }

    public Customer saveCustomer(Customer customer) {
        List<Customer> customers = loadAllCustomers();

        // Assign ID if new customer
        if (customer.getCustomerId() == 0) {
            int maxId = 0;
            for (Customer c : customers) {
                if (c.getCustomerId() > maxId) {
                    maxId = c.getCustomerId();
                }
            }
            customer.setCustomerId(maxId + 1);
        } else {
            // Remove existing customer with same ID
            customers.removeIf(c -> c.getCustomerId() == customer.getCustomerId());
        }

        customers.add(customer);
        saveAllCustomers(customers);
        return customer;
    }

    private void saveAllCustomers(List<Customer> customers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CUSTOMERS_FILE))) {
            for (Customer customer : customers) {
                writer.write(String.format("%d|%s|%s|%d|%s|%s|%s%n",
                        customer.getCustomerId(),
                        customer.getName(),
                        customer.getContactNumber(),
                        customer.getDriverLicenseNumber(),
                        customer.getEmail(),
                        customer.getPassword(),
                        customer.getAddress() != null ? customer.getAddress() : ""));
            }
        } catch (IOException e) {
            System.err.println("Error saving customers to file");
            e.printStackTrace();
        }
    }

    public Optional<Customer> findCustomerByEmail(String email) {
        List<Customer> customers = loadAllCustomers();
        return customers.stream()
                .filter(c -> c.getEmail().equals(email))
                .findFirst();
    }

    // Delete a customer by ID
    public boolean deleteCustomer(int customerId) {
        List<Customer> customers = loadAllCustomers();
        boolean removed = customers.removeIf(c -> c.getCustomerId() == customerId);
        if (removed) {
            saveAllCustomers(customers);
        }
        return removed;
    }

    // Update a customer (functionally same as save but more explicit)
    public Customer updateCustomer(Customer customer) {
        if (customer.getCustomerId() == 0) {
            throw new IllegalArgumentException("Cannot update customer without ID");
        }
        return saveCustomer(customer);
    }

    // Vehicle methods
    public List<Vehicle> loadAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(VEHICLES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    Vehicle vehicle = new Vehicle();
                    vehicle.setVehicleID(parts[0]);
                    vehicle.setBrand(parts[1]);
                    vehicle.setModel(parts[2]);
                    vehicle.setRentPrice(Double.parseDouble(parts[3]));

                    // Set image path if available
                    if (parts.length > 4 && !parts[4].isEmpty()) {
                        vehicle.setImagePath(parts[4]);
                    }

                    // Set vehicle type if available (added in newer format)
                    if (parts.length > 5 && !parts[5].isEmpty()) {
                        try {
                            int vehicleType = Integer.parseInt(parts[5]);
                            // Set vehicle type using reflection to avoid compile errors
                            try {
                                vehicle.getClass().getMethod("setVehicleType", int.class).invoke(vehicle, vehicleType);
                            } catch (Exception e) {
                                System.out
                                        .println("Could not set vehicle type, field may not exist: " + e.getMessage());
                            }

                            // Set type-specific properties based on vehicle type
                            if (vehicleType == 1 && parts.length > 7) { // Car
                                try {
                                    vehicle.getClass().getMethod("setNumberOfDoors", int.class)
                                            .invoke(vehicle, Integer.parseInt(parts[6]));
                                    vehicle.getClass().getMethod("setTransmissionType", String.class)
                                            .invoke(vehicle, parts[7]);
                                } catch (Exception e) {
                                    System.out.println("Error setting car properties: " + e.getMessage());
                                }
                            } else if (vehicleType == 2 && parts.length > 6) { // Van
                                try {
                                    vehicle.getClass().getMethod("setCargoCapacity", double.class)
                                            .invoke(vehicle, Double.parseDouble(parts[6]));
                                } catch (Exception e) {
                                    System.out.println("Error setting van properties: " + e.getMessage());
                                }
                            } else if (vehicleType == 3 && parts.length > 6) { // Bike
                                try {
                                    vehicle.getClass().getMethod("setEngineCapacity", int.class)
                                            .invoke(vehicle, Integer.parseInt(parts[6]));
                                } catch (Exception e) {
                                    System.out.println("Error setting bike properties: " + e.getMessage());
                                }
                            } else if (vehicleType == 4 && parts.length > 6) { // Bus
                                try {
                                    vehicle.getClass().getMethod("setSeatingCapacity", int.class)
                                            .invoke(vehicle, Integer.parseInt(parts[6]));
                                } catch (Exception e) {
                                    System.out.println("Error setting bus properties: " + e.getMessage());
                                }
                            } else if (vehicleType == 5 && parts.length > 6) { // Lorry
                                try {
                                    vehicle.getClass().getMethod("setMaxLoad", double.class)
                                            .invoke(vehicle, Double.parseDouble(parts[6]));
                                } catch (Exception e) {
                                    System.out.println("Error setting lorry properties: " + e.getMessage());
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Error parsing vehicle type: " + parts[5]);
                        }
                    }

                    vehicles.add(vehicle);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading vehicles from file");
            e.printStackTrace();
        }
        return vehicles;
    }

    public Vehicle saveVehicle(Vehicle vehicle) {
        List<Vehicle> vehicles = loadAllVehicles();

        // Remove existing vehicle with same ID if it exists
        vehicles.removeIf(v -> v.getVehicleID().equals(vehicle.getVehicleID()));

        vehicles.add(vehicle);
        saveAllVehicles(vehicles);
        return vehicle;
    }

    private void saveAllVehicles(List<Vehicle> vehicles) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VEHICLES_FILE))) {
            for (Vehicle vehicle : vehicles) {
                StringBuilder sb = new StringBuilder();
                sb.append(vehicle.getVehicleID()).append("|");
                sb.append(vehicle.getBrand()).append("|");
                sb.append(vehicle.getModel()).append("|");
                sb.append(String.format("%.2f", vehicle.getRentPrice())).append("|");
                sb.append(vehicle.getImagePath() != null ? vehicle.getImagePath() : "").append("|");

                // Get vehicle type using reflection to avoid compile errors
                int vehicleType = 0;
                try {
                    Object typeObj = vehicle.getClass().getMethod("getVehicleType").invoke(vehicle);
                    if (typeObj != null) {
                        vehicleType = (int) typeObj;
                    }
                } catch (Exception e) {
                    // Determine vehicle type based on class properties
                    if (vehicle.getClass().getName().contains("Car") || hasProperyMethod(vehicle, "getNumberOfDoors")) {
                        vehicleType = 1;
                    } else if (vehicle.getClass().getName().contains("Van")
                            || hasProperyMethod(vehicle, "getCargoCapacity")) {
                        vehicleType = 2;
                    } else if (vehicle.getClass().getName().contains("Bike")
                            || hasProperyMethod(vehicle, "getEngineCapacity")) {
                        vehicleType = 3;
                    } else if (vehicle.getClass().getName().contains("Bus")
                            || hasProperyMethod(vehicle, "getSeatingCapacity")) {
                        vehicleType = 4;
                    } else if (vehicle.getClass().getName().contains("Lorry")
                            || hasProperyMethod(vehicle, "getMaxLoad")) {
                        vehicleType = 5;
                    }
                }
                sb.append(vehicleType).append("|");

                // Add type-specific properties
                if (vehicleType == 1) { // Car
                    try {
                        sb.append(vehicle.getClass().getMethod("getNumberOfDoors").invoke(vehicle)).append("|");
                        sb.append(vehicle.getClass().getMethod("getTransmissionType").invoke(vehicle)).append("|");
                    } catch (Exception e) {
                        sb.append("4|Manual|"); // Default values
                    }
                } else if (vehicleType == 2) { // Van
                    try {
                        sb.append(vehicle.getClass().getMethod("getCargoCapacity").invoke(vehicle)).append("|");
                    } catch (Exception e) {
                        sb.append("500|"); // Default value
                    }
                } else if (vehicleType == 3) { // Bike
                    try {
                        sb.append(vehicle.getClass().getMethod("getEngineCapacity").invoke(vehicle)).append("|");
                    } catch (Exception e) {
                        sb.append("150|"); // Default value
                    }
                } else if (vehicleType == 4) { // Bus
                    try {
                        sb.append(vehicle.getClass().getMethod("getSeatingCapacity").invoke(vehicle)).append("|");
                    } catch (Exception e) {
                        sb.append("40|"); // Default value
                    }
                } else if (vehicleType == 5) { // Lorry
                    try {
                        sb.append(vehicle.getClass().getMethod("getMaxLoad").invoke(vehicle)).append("|");
                    } catch (Exception e) {
                        sb.append("5.0|"); // Default value
                    }
                }

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving vehicles to file");
            e.printStackTrace();
        }
    }

    // Helper method to check if a vehicle has a specific property method
    private boolean hasProperyMethod(Vehicle vehicle, String methodName) {
        try {
            vehicle.getClass().getMethod(methodName);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    // Delete a vehicle by ID
    public boolean deleteVehicle(String vehicleId) {
        List<Vehicle> vehicles = loadAllVehicles();
        boolean removed = vehicles.removeIf(v -> v.getVehicleID().equals(vehicleId));
        if (removed) {
            saveAllVehicles(vehicles);
        }
        return removed;
    }

    // Update a vehicle (functionally same as save but more explicit)
    public Vehicle updateVehicle(Vehicle vehicle) {
        if (vehicle.getVehicleID() == null || vehicle.getVehicleID().isEmpty()) {
            throw new IllegalArgumentException("Cannot update vehicle without ID");
        }
        return saveVehicle(vehicle);
    }

    // Event methods
    public List<Event> loadAllEvents() {
        List<Event> events = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(EVENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    Event event = new Event();
                    event.setId(Long.parseLong(parts[0]));
                    event.setName(parts[1]);
                    event.setDescription(parts[2]);
                    event.setPrice(Double.parseDouble(parts[3]));
                    event.setEventType(parts[4]);
                    event.setDurationHours(Integer.parseInt(parts[5]));
                    event.setActive(Boolean.parseBoolean(parts[6]));

                    if (parts.length > 7) {
                        event.setImagePath(parts[7]);
                    }

                    // Handle dates if present
                    if (parts.length > 8 && !parts[8].isEmpty()) {
                        try {
                            event.setStartDate(dateFormat.parse(parts[8]));
                        } catch (ParseException e) {
                            System.err.println("Error parsing start date: " + parts[8]);
                        }
                    }

                    if (parts.length > 9 && !parts[9].isEmpty()) {
                        try {
                            event.setEndDate(dateFormat.parse(parts[9]));
                        } catch (ParseException e) {
                            System.err.println("Error parsing end date: " + parts[9]);
                        }
                    }

                    // Handle associated vehicles if present
                    if (parts.length > 10 && !parts[10].isEmpty()) {
                        String[] vehicleIds = parts[10].split(",");
                        List<String> vehicleIdList = new ArrayList<>();
                        for (String id : vehicleIds) {
                            if (!id.trim().isEmpty()) {
                                vehicleIdList.add(id.trim());
                            }
                        }
                        event.setVehicleIds(vehicleIdList);
                    }

                    events.add(event);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading events from file");
            e.printStackTrace();
        }
        return events;
    }

    public Event saveEvent(Event event) {
        List<Event> events = loadAllEvents();

        // Assign ID if new event
        if (event.getId() == null) {
            long maxId = 0;
            for (Event e : events) {
                if (e.getId() > maxId) {
                    maxId = e.getId();
                }
            }
            event.setId(maxId + 1);
        } else {
            // Remove existing event with same ID
            events.removeIf(e -> e.getId().equals(event.getId()));
        }

        events.add(event);
        saveAllEvents(events);
        return event;
    }

    private void saveAllEvents(List<Event> events) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(EVENTS_FILE))) {
            for (Event event : events) {
                // Build vehicle IDs string
                StringBuilder vehicleIds = new StringBuilder();
                if (event.getVehicleIds() != null && !event.getVehicleIds().isEmpty()) {
                    for (String id : event.getVehicleIds()) {
                        if (vehicleIds.length() > 0) {
                            vehicleIds.append(",");
                        }
                        vehicleIds.append(id);
                    }
                } else if (event.getVehicles() != null && !event.getVehicles().isEmpty()) {
                    boolean first = true;
                    for (Vehicle vehicle : event.getVehicles()) {
                        if (!first) {
                            vehicleIds.append(",");
                        }
                        vehicleIds.append(vehicle.getVehicleID());
                        first = false;
                    }
                }

                writer.write(String.format("%d|%s|%s|%.2f|%s|%d|%b|%s|%s|%s|%s%n",
                        event.getId(),
                        event.getName(),
                        event.getDescription(),
                        event.getPrice(),
                        event.getEventType(),
                        event.getDurationHours(),
                        event.isActive(),
                        event.getImagePath() != null ? event.getImagePath() : "",
                        event.getStartDate() != null ? dateFormat.format(event.getStartDate()) : "",
                        event.getEndDate() != null ? dateFormat.format(event.getEndDate()) : "",
                        vehicleIds.toString()));
            }
        } catch (IOException e) {
            System.err.println("Error saving events to file");
            e.printStackTrace();
        }
    }

    // Delete an event by ID
    public boolean deleteEvent(Long eventId) {
        List<Event> events = loadAllEvents();
        boolean removed = events.removeIf(e -> e.getId().equals(eventId));
        if (removed) {
            saveAllEvents(events);
        }
        return removed;
    }

    // Update an event (functionally same as save but more explicit)
    public Event updateEvent(Event event) {
        if (event.getId() == null) {
            throw new IllegalArgumentException("Cannot update event without ID");
        }
        return saveEvent(event);
    }

    // Discount methods
    public List<Discount> loadAllDiscounts() {
        List<Discount> discounts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DISCOUNTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    Discount discount = new Discount();
                    discount.setId(Long.parseLong(parts[0]));
                    discount.setName(parts[1]);
                    discount.setDescription(parts[2]);
                    discount.setDiscountPercentage(Double.parseDouble(parts[3]));
                    discount.setDiscountType(Discount.DiscountType.valueOf(parts[4]));
                    discount.setActive(Boolean.parseBoolean(parts[5]));

                    if (parts.length > 6 && !parts[6].isEmpty()) {
                        discount.setStartDate(java.time.LocalDate.parse(parts[6]));
                    }

                    if (parts.length > 7 && !parts[7].isEmpty()) {
                        discount.setEndDate(java.time.LocalDate.parse(parts[7]));
                    }

                    if (parts.length > 8 && !parts[8].isEmpty()) {
                        discount.setMinimumRides(Integer.parseInt(parts[8]));
                    }

                    if (parts.length > 9) {
                        discount.setApplyToAllVehicles(Boolean.parseBoolean(parts[9]));
                    }

                    discounts.add(discount);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading discounts from file");
            e.printStackTrace();
        }
        return discounts;
    }

    public Discount saveDiscount(Discount discount) {
        List<Discount> discounts = loadAllDiscounts();

        // Assign ID if new discount
        if (discount.getId() == null) {
            long maxId = 0;
            for (Discount d : discounts) {
                if (d.getId() > maxId) {
                    maxId = d.getId();
                }
            }
            discount.setId(maxId + 1);
        } else {
            // Remove existing discount with same ID
            discounts.removeIf(d -> d.getId().equals(discount.getId()));
        }

        discounts.add(discount);
        saveAllDiscounts(discounts);
        return discount;
    }

    private void saveAllDiscounts(List<Discount> discounts) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DISCOUNTS_FILE))) {
            for (Discount discount : discounts) {
                writer.write(String.format("%d|%s|%s|%.2f|%s|%b|%s|%s|%s|%b%n",
                        discount.getId(),
                        discount.getName(),
                        discount.getDescription(),
                        discount.getDiscountPercentage(),
                        discount.getDiscountType(),
                        discount.isActive(),
                        discount.getStartDate() != null ? discount.getStartDate().toString() : "",
                        discount.getEndDate() != null ? discount.getEndDate().toString() : "",
                        discount.getMinimumRides() != null ? discount.getMinimumRides().toString() : "",
                        discount.isApplyToAllVehicles()));
            }
        } catch (IOException e) {
            System.err.println("Error saving discounts to file");
            e.printStackTrace();
        }
    }

    // Delete a discount by ID
    public boolean deleteDiscount(Long discountId) {
        List<Discount> discounts = loadAllDiscounts();
        boolean removed = discounts.removeIf(d -> d.getId().equals(discountId));
        if (removed) {
            saveAllDiscounts(discounts);
        }
        return removed;
    }

    // Update a discount (functionally same as save but more explicit)
    public Discount updateDiscount(Discount discount) {
        if (discount.getId() == null) {
            throw new IllegalArgumentException("Cannot update discount without ID");
        }
        return saveDiscount(discount);
    }

    // Staff methods
    public List<Staff> loadAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(STAFF_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    Staff staff = new Staff();
                    staff.setId(Long.parseLong(parts[0]));
                    staff.setName(parts[1]);
                    staff.setEmail(parts[2]);
                    staff.setPhone(parts[3]);
                    staff.setDepartment(parts[4]);

                    staffList.add(staff);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading staff from file");
            e.printStackTrace();
        }
        return staffList;
    }

    public Staff saveStaff(Staff staff) {
        List<Staff> staffList = loadAllStaff();

        // Assign ID if new staff
        if (staff.getId() == null) {
            long maxId = 0;
            for (Staff s : staffList) {
                if (s.getId() > maxId) {
                    maxId = s.getId();
                }
            }
            staff.setId(maxId + 1);
        } else {
            // Remove existing staff with same ID
            staffList.removeIf(s -> s.getId().equals(staff.getId()));
        }

        staffList.add(staff);
        saveAllStaff(staffList);
        return staff;
    }

    private void saveAllStaff(List<Staff> staffList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STAFF_FILE))) {
            for (Staff staff : staffList) {
                writer.write(String.format("%d|%s|%s|%s|%s%n",
                        staff.getId(),
                        staff.getName(),
                        staff.getEmail(),
                        staff.getPhone(),
                        staff.getDepartment()));
            }
        } catch (IOException e) {
            System.err.println("Error saving staff to file");
            e.printStackTrace();
        }
    }

    // Delete a staff by ID
    public boolean deleteStaff(Long staffId) {
        List<Staff> staffList = loadAllStaff();
        boolean removed = staffList.removeIf(s -> s.getId().equals(staffId));
        if (removed) {
            saveAllStaff(staffList);
        }
        return removed;
    }

    // Update a staff (functionally same as save but more explicit)
    public Staff updateStaff(Staff staff) {
        if (staff.getId() == null) {
            throw new IllegalArgumentException("Cannot update staff without ID");
        }
        return saveStaff(staff);
    }

    // EventBooking methods
    public List<EventBooking> loadAllEventBookings() {
        List<EventBooking> bookings = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(EVENT_BOOKINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    EventBooking booking = new EventBooking();
                    booking.setId(Long.parseLong(parts[0]));
                    booking.setBookingId(parts[1]);

                    // Set event ID reference
                    Long eventId = Long.parseLong(parts[2]);
                    Event event = new Event();
                    event.setId(eventId);
                    booking.setEvent(event);

                    // Set customer ID reference
                    int customerId = Integer.parseInt(parts[3]);
                    Customer customer = new Customer();
                    customer.setCustomerId(customerId);
                    booking.setCustomer(customer);

                    // Set date
                    if (!parts[4].isEmpty()) {
                        try {
                            booking.setEventDate(dateFormat.parse(parts[4]));
                        } catch (ParseException e) {
                            System.err.println("Error parsing event date: " + parts[4]);
                        }
                    }

                    if (parts.length > 5) {
                        booking.setStatus(parts[5]);
                    }

                    if (parts.length > 6) {
                        booking.setSpecialRequirements(parts[6]);
                    }

                    bookings.add(booking);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading event bookings from file");
            e.printStackTrace();
        }
        return bookings;
    }

    public EventBooking saveEventBooking(EventBooking booking) {
        List<EventBooking> bookings = loadAllEventBookings();

        // Assign ID if new booking
        if (booking.getId() == null) {
            long maxId = 0;
            for (EventBooking b : bookings) {
                if (b.getId() > maxId) {
                    maxId = b.getId();
                }
            }
            booking.setId(maxId + 1);
        } else {
            // Remove existing booking with same ID
            bookings.removeIf(b -> b.getId().equals(booking.getId()));
        }

        bookings.add(booking);
        saveAllEventBookings(bookings);
        return booking;
    }

    private void saveAllEventBookings(List<EventBooking> bookings) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(EVENT_BOOKINGS_FILE))) {
            for (EventBooking booking : bookings) {
                writer.write(String.format("%d|%s|%d|%d|%s|%s|%s%n",
                        booking.getId(),
                        booking.getBookingId(),
                        booking.getEvent().getId(),
                        booking.getCustomer().getCustomerId(),
                        booking.getEventDate() != null ? dateFormat.format(booking.getEventDate()) : "",
                        booking.getStatus() != null ? booking.getStatus() : "",
                        booking.getSpecialRequirements() != null ? booking.getSpecialRequirements() : ""));
            }
        } catch (IOException e) {
            System.err.println("Error saving event bookings to file");
            e.printStackTrace();
        }
    }

    // Delete an event booking by ID
    public boolean deleteEventBooking(Long bookingId) {
        List<EventBooking> bookings = loadAllEventBookings();
        boolean removed = bookings.removeIf(b -> b.getId().equals(bookingId));
        if (removed) {
            saveAllEventBookings(bookings);
        }
        return removed;
    }

    // Update an event booking (functionally same as save but more explicit)
    public EventBooking updateEventBooking(EventBooking booking) {
        if (booking.getId() == null) {
            throw new IllegalArgumentException("Cannot update event booking without ID");
        }
        return saveEventBooking(booking);
    }

    // PurchasedVehicle methods
    public List<PurchasedVehicle> loadAllPurchasedVehicles() {
        List<PurchasedVehicle> purchases = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(PURCHASED_VEHICLES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    PurchasedVehicle purchase = new PurchasedVehicle();
                    purchase.setId(Long.parseLong(parts[0]));
                    purchase.setPurchaseId(parts[1]);

                    // Set vehicle ID reference and look up full vehicle details
                    String vehicleId = parts[2];

                    // Try to find the complete vehicle in the vehicles file first
                    Optional<Vehicle> existingVehicle = findVehicleById(vehicleId);

                    if (existingVehicle.isPresent()) {
                        // Use the complete vehicle from storage
                        purchase.setVehicle(existingVehicle.get());
                    } else {
                        // Create a new vehicle with at least the ID
                        Vehicle vehicle = new Vehicle();
                        vehicle.setVehicleID(vehicleId);

                        // If we have more parts for vehicle details, use them (from extended format)
                        if (parts.length > 9) {
                            try {
                                vehicle.setBrand(parts[9]);
                                vehicle.setModel(parts[10]);
                                if (parts.length > 11) {
                                    vehicle.setRentPrice(Double.parseDouble(parts[11]));
                                }
                                if (parts.length > 12) {
                                    vehicle.setImagePath(parts[12]);
                                }

                                // Set vehicle type if available
                                if (parts.length > 13) {
                                    try {
                                        int vehicleType = Integer.parseInt(parts[13]);
                                        try {
                                            vehicle.getClass().getMethod("setVehicleType", int.class).invoke(vehicle,
                                                    vehicleType);
                                        } catch (Exception e) {
                                            System.out.println(
                                                    "Could not set vehicle type on rental vehicle: " + e.getMessage());
                                        }

                                        // Set type-specific properties
                                        if (vehicleType == 1 && parts.length > 15) { // Car
                                            try {
                                                vehicle.getClass().getMethod("setNumberOfDoors", int.class)
                                                        .invoke(vehicle, Integer.parseInt(parts[14]));
                                                vehicle.getClass().getMethod("setTransmissionType", String.class)
                                                        .invoke(vehicle, parts[15]);
                                            } catch (Exception e) {
                                                System.out.println(
                                                        "Error setting car properties for rental: " + e.getMessage());
                                            }
                                        } else if (vehicleType == 2 && parts.length > 14) { // Van
                                            try {
                                                vehicle.getClass().getMethod("setCargoCapacity", double.class)
                                                        .invoke(vehicle, Double.parseDouble(parts[14]));
                                            } catch (Exception e) {
                                                System.out.println(
                                                        "Error setting van properties for rental: " + e.getMessage());
                                            }
                                        } else if (vehicleType == 3 && parts.length > 14) { // Bike
                                            try {
                                                vehicle.getClass().getMethod("setEngineCapacity", int.class)
                                                        .invoke(vehicle, Integer.parseInt(parts[14]));
                                            } catch (Exception e) {
                                                System.out.println(
                                                        "Error setting bike properties for rental: " + e.getMessage());
                                            }
                                        } else if (vehicleType == 4 && parts.length > 14) { // Bus
                                            try {
                                                vehicle.getClass().getMethod("setSeatingCapacity", int.class)
                                                        .invoke(vehicle, Integer.parseInt(parts[14]));
                                            } catch (Exception e) {
                                                System.out.println(
                                                        "Error setting bus properties for rental: " + e.getMessage());
                                            }
                                        } else if (vehicleType == 5 && parts.length > 14) { // Lorry
                                            try {
                                                vehicle.getClass().getMethod("setMaxLoad", double.class)
                                                        .invoke(vehicle, Double.parseDouble(parts[14]));
                                            } catch (Exception e) {
                                                System.out.println(
                                                        "Error setting lorry properties for rental: " + e.getMessage());
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        System.out.println("Error parsing vehicle type for rental: " + parts[13]);
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println(
                                        "Error setting extended vehicle details for rental: " + e.getMessage());
                            }
                        }

                        purchase.setVehicle(vehicle);
                    }

                    // Set customer ID reference
                    int customerId = Integer.parseInt(parts[3]);
                    Customer customer = new Customer();
                    customer.setCustomerId(customerId);
                    purchase.setCustomer(customer);

                    purchase.setRentalDays(Integer.parseInt(parts[4]));

                    // Set purchase date
                    if (parts.length > 5 && !parts[5].isEmpty()) {
                        try {
                            purchase.setPurchaseDate(dateFormat.parse(parts[5]));
                        } catch (ParseException e) {
                            System.err.println("Error parsing purchase date: " + parts[5]);
                        }
                    }

                    if (parts.length > 6) {
                        purchase.setRented(Boolean.parseBoolean(parts[6]));
                    }

                    if (parts.length > 7) {
                        purchase.setCustomerName(parts[7]);
                    }

                    if (parts.length > 8) {
                        purchase.setContactNumber(parts[8]);
                    }

                    purchases.add(purchase);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading purchased vehicles from file");
            e.printStackTrace();
        }
        return purchases;
    }

    private void saveAllPurchasedVehicles(List<PurchasedVehicle> purchases) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PURCHASED_VEHICLES_FILE))) {
            for (PurchasedVehicle purchase : purchases) {
                StringBuilder sb = new StringBuilder();

                // Base purchase details
                sb.append(purchase.getId()).append("|");
                sb.append(purchase.getPurchaseId()).append("|");

                // Vehicle ID
                Vehicle vehicle = purchase.getVehicle();
                String vehicleId = (vehicle != null) ? vehicle.getVehicleID() : "";
                sb.append(vehicleId).append("|");

                // Customer and rental details
                sb.append(purchase.getCustomer().getCustomerId()).append("|");
                sb.append(purchase.getRentalDays()).append("|");
                sb.append(purchase.getPurchaseDate() != null ? dateFormat.format(purchase.getPurchaseDate()) : "")
                        .append("|");
                sb.append(purchase.isRented()).append("|");
                sb.append(purchase.getCustomerName() != null ? purchase.getCustomerName() : "").append("|");
                sb.append(purchase.getContactNumber() != null ? purchase.getContactNumber() : "").append("|");

                // Store complete vehicle details to ensure they're preserved
                if (vehicle != null) {
                    sb.append(vehicle.getBrand() != null ? vehicle.getBrand() : "Unknown").append("|");
                    sb.append(vehicle.getModel() != null ? vehicle.getModel() : "Unknown").append("|");
                    sb.append(vehicle.getRentPrice()).append("|");
                    sb.append(vehicle.getImagePath() != null ? vehicle.getImagePath() : "").append("|");

                    // Get vehicle type using reflection
                    int vehicleType = 0;
                    try {
                        Object typeObj = vehicle.getClass().getMethod("getVehicleType").invoke(vehicle);
                        if (typeObj != null) {
                            vehicleType = (int) typeObj;
                        }
                    } catch (Exception e) {
                        // Determine vehicle type based on class properties
                        if (vehicle.getClass().getName().contains("Car")
                                || hasProperyMethod(vehicle, "getNumberOfDoors")) {
                            vehicleType = 1;
                        } else if (vehicle.getClass().getName().contains("Van")
                                || hasProperyMethod(vehicle, "getCargoCapacity")) {
                            vehicleType = 2;
                        } else if (vehicle.getClass().getName().contains("Bike")
                                || hasProperyMethod(vehicle, "getEngineCapacity")) {
                            vehicleType = 3;
                        } else if (vehicle.getClass().getName().contains("Bus")
                                || hasProperyMethod(vehicle, "getSeatingCapacity")) {
                            vehicleType = 4;
                        } else if (vehicle.getClass().getName().contains("Lorry")
                                || hasProperyMethod(vehicle, "getMaxLoad")) {
                            vehicleType = 5;
                        }
                    }
                    sb.append(vehicleType).append("|");

                    // Add type-specific properties
                    if (vehicleType == 1) { // Car
                        try {
                            sb.append(vehicle.getClass().getMethod("getNumberOfDoors").invoke(vehicle)).append("|");
                            sb.append(vehicle.getClass().getMethod("getTransmissionType").invoke(vehicle)).append("|");
                        } catch (Exception e) {
                            sb.append("4|Manual|"); // Default values
                        }
                    } else if (vehicleType == 2) { // Van
                        try {
                            sb.append(vehicle.getClass().getMethod("getCargoCapacity").invoke(vehicle)).append("|");
                        } catch (Exception e) {
                            sb.append("500|"); // Default value
                        }
                    } else if (vehicleType == 3) { // Bike
                        try {
                            sb.append(vehicle.getClass().getMethod("getEngineCapacity").invoke(vehicle)).append("|");
                        } catch (Exception e) {
                            sb.append("150|"); // Default value
                        }
                    } else if (vehicleType == 4) { // Bus
                        try {
                            sb.append(vehicle.getClass().getMethod("getSeatingCapacity").invoke(vehicle)).append("|");
                        } catch (Exception e) {
                            sb.append("40|"); // Default value
                        }
                    } else if (vehicleType == 5) { // Lorry
                        try {
                            sb.append(vehicle.getClass().getMethod("getMaxLoad").invoke(vehicle)).append("|");
                        } catch (Exception e) {
                            sb.append("5.0|"); // Default value
                        }
                    }
                }

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving purchased vehicles to file");
            e.printStackTrace();
        }
    }

    // Helper method to find a vehicle by ID directly for use in
    // loadAllPurchasedVehicles
    private Optional<Vehicle> findVehicleById(String vehicleId) {
        return loadAllVehicles().stream()
                .filter(v -> v.getVehicleID().equals(vehicleId))
                .findFirst();
    }

    public PurchasedVehicle savePurchasedVehicle(PurchasedVehicle purchase) {
        List<PurchasedVehicle> purchases = loadAllPurchasedVehicles();

        // Assign ID if new purchase
        if (purchase.getId() == null) {
            long maxId = 0;
            for (PurchasedVehicle p : purchases) {
                if (p.getId() > maxId) {
                    maxId = p.getId();
                }
            }
            purchase.setId(maxId + 1);
        } else {
            // Remove existing purchase with same ID
            purchases.removeIf(p -> p.getId().equals(purchase.getId()));
        }

        purchases.add(purchase);
        saveAllPurchasedVehicles(purchases);
        return purchase;
    }

    // Delete a purchased vehicle by ID
    public boolean deletePurchasedVehicle(Long purchaseId) {
        List<PurchasedVehicle> purchases = loadAllPurchasedVehicles();
        boolean removed = purchases.removeIf(p -> p.getId().equals(purchaseId));
        if (removed) {
            saveAllPurchasedVehicles(purchases);
        }
        return removed;
    }

    // Update a purchased vehicle (functionally same as save but more explicit)
    public PurchasedVehicle updatePurchasedVehicle(PurchasedVehicle purchase) {
        if (purchase.getId() == null) {
            throw new IllegalArgumentException("Cannot update purchased vehicle without ID");
        }
        return savePurchasedVehicle(purchase);
    }

    // Payment methods
    public List<Payment> loadAllPayments() {
        List<Payment> payments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(PAYMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    Payment payment = new Payment();
                    payment.setPaymentId(parts[0]);
                    payment.setPaymentMethod(parts[1]);
                    payment.setAmount(Double.parseDouble(parts[2]));
                    payment.setStatus(parts[3]);

                    // Parse payment date
                    try {
                        if (!parts[4].isEmpty()) {
                            payment.setPaymentDate(timestampFormat.parse(parts[4]));
                        }
                    } catch (ParseException e) {
                        System.err.println("Error parsing payment date: " + parts[4]);
                    }

                    // Set card details if present
                    if (parts.length > 6 && "CARD".equalsIgnoreCase(parts[1])) {
                        payment.setCardHolderName(parts[6]);
                        if (parts.length > 7) {
                            payment.setCardNumber(parts[7]);
                        }
                        if (parts.length > 8) {
                            payment.setCardExpiry(parts[8]);
                        }
                        if (parts.length > 9) {
                            payment.setCardType(parts[9]);
                        }
                    }

                    // Set purchased vehicle reference if available
                    String purchaseId = parts[5];
                    if (purchaseId != null && !purchaseId.isEmpty()) {
                        Optional<PurchasedVehicle> purchasedVehicle = findPurchasedVehicleByPurchaseId(purchaseId);
                        purchasedVehicle.ifPresent(payment::setPurchasedVehicle);
                    }

                    payments.add(payment);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading payments from file");
            e.printStackTrace();
        }
        return payments;
    }

    public Payment savePayment(Payment payment) {
        List<Payment> payments = loadAllPayments();

        // Remove existing payment with same ID if it exists
        payments.removeIf(p -> p.getPaymentId().equals(payment.getPaymentId()));

        payments.add(payment);
        saveAllPayments(payments);
        return payment;
    }

    private void saveAllPayments(List<Payment> payments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PAYMENTS_FILE))) {
            for (Payment payment : payments) {
                StringBuilder sb = new StringBuilder();
                sb.append(payment.getPaymentId()).append("|");
                sb.append(payment.getPaymentMethod()).append("|");
                sb.append(payment.getAmount()).append("|");
                sb.append(payment.getStatus()).append("|");
                sb.append(payment.getPaymentDate() != null ? timestampFormat.format(payment.getPaymentDate()) : "")
                        .append("|");
                sb.append(payment.getPurchasedVehicle() != null ? payment.getPurchasedVehicle().getPurchaseId() : "")
                        .append("|");

                // Card-specific details
                if ("CARD".equalsIgnoreCase(payment.getPaymentMethod())) {
                    sb.append(payment.getCardHolderName() != null ? payment.getCardHolderName() : "").append("|");
                    sb.append(payment.getCardNumber() != null ? payment.getCardNumber() : "").append("|");
                    sb.append(payment.getCardExpiry() != null ? payment.getCardExpiry() : "").append("|");
                    sb.append(payment.getCardType() != null ? payment.getCardType() : "");
                }

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving payments to file");
            e.printStackTrace();
        }
    }

    // Delete a payment by ID
    public boolean deletePayment(String paymentId) {
        List<Payment> payments = loadAllPayments();
        boolean removed = payments.removeIf(p -> p.getPaymentId().equals(paymentId));
        if (removed) {
            saveAllPayments(payments);
        }
        return removed;
    }

    // Update a payment (functionally same as save but more explicit)
    public Payment updatePayment(Payment payment) {
        if (payment.getPaymentId() == null || payment.getPaymentId().isEmpty()) {
            throw new IllegalArgumentException("Cannot update payment without ID");
        }
        return savePayment(payment);
    }

    // Helper method to find a purchased vehicle by its purchase ID
    public Optional<PurchasedVehicle> findPurchasedVehicleByPurchaseId(String purchaseId) {
        return loadAllPurchasedVehicles().stream()
                .filter(p -> p.getPurchaseId() != null && p.getPurchaseId().equals(purchaseId))
                .findFirst();
    }
}