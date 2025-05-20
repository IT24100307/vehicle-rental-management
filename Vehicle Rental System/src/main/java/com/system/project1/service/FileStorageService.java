package com.system.project1.service;

import com.system.project1.entity.*;
import com.system.project1.util.FileStorageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service to handle file-based storage operations for all entities
 */
@Service
public class FileStorageService {

    @Autowired
    private FileStorageManager fileStorageManager;

    // Customer operations
    public List<Customer> getAllCustomers() {
        return fileStorageManager.loadAllCustomers();
    }

    public Customer saveCustomer(Customer customer) {
        return fileStorageManager.saveCustomer(customer);
    }

    public Optional<Customer> findCustomerByEmail(String email) {
        return fileStorageManager.findCustomerByEmail(email);
    }

    public boolean deleteCustomer(int customerId) {
        return fileStorageManager.deleteCustomer(customerId);
    }

    public Customer updateCustomer(Customer customer) {
        return fileStorageManager.updateCustomer(customer);
    }

    // Vehicle operations
    public List<Vehicle> getAllVehicles() {
        return fileStorageManager.loadAllVehicles();
    }

    public Vehicle saveVehicle(Vehicle vehicle) {
        return fileStorageManager.saveVehicle(vehicle);
    }

    public void saveAllVehicles(List<Vehicle> vehicles) {
        for (Vehicle vehicle : vehicles) {
            fileStorageManager.saveVehicle(vehicle);
        }
    }

    public boolean deleteVehicle(String vehicleId) {
        return fileStorageManager.deleteVehicle(vehicleId);
    }

    public Vehicle updateVehicle(Vehicle vehicle) {
        return fileStorageManager.updateVehicle(vehicle);
    }

    // Event operations
    public List<Event> getAllEvents() {
        return fileStorageManager.loadAllEvents();
    }

    public Event saveEvent(Event event) {
        return fileStorageManager.saveEvent(event);
    }

    public void saveAllEvents(List<Event> events) {
        for (Event event : events) {
            fileStorageManager.saveEvent(event);
        }
    }

    public boolean deleteEvent(Long eventId) {
        return fileStorageManager.deleteEvent(eventId);
    }

    public Event updateEvent(Event event) {
        return fileStorageManager.updateEvent(event);
    }

    // Discount operations
    public List<Discount> getAllDiscounts() {
        return fileStorageManager.loadAllDiscounts();
    }

    public Discount saveDiscount(Discount discount) {
        return fileStorageManager.saveDiscount(discount);
    }

    public void saveAllDiscounts(List<Discount> discounts) {
        for (Discount discount : discounts) {
            fileStorageManager.saveDiscount(discount);
        }
    }

    public boolean deleteDiscount(Long discountId) {
        return fileStorageManager.deleteDiscount(discountId);
    }

    public Discount updateDiscount(Discount discount) {
        return fileStorageManager.updateDiscount(discount);
    }

    // Staff operations
    public List<Staff> getAllStaff() {
        return fileStorageManager.loadAllStaff();
    }

    public Staff saveStaff(Staff staff) {
        return fileStorageManager.saveStaff(staff);
    }

    public void saveAllStaff(List<Staff> staffList) {
        for (Staff staff : staffList) {
            fileStorageManager.saveStaff(staff);
        }
    }

    public boolean deleteStaff(Long staffId) {
        return fileStorageManager.deleteStaff(staffId);
    }

    public Staff updateStaff(Staff staff) {
        return fileStorageManager.updateStaff(staff);
    }

    // EventBooking operations
    public List<EventBooking> getAllEventBookings() {
        return fileStorageManager.loadAllEventBookings();
    }

    public EventBooking saveEventBooking(EventBooking booking) {
        return fileStorageManager.saveEventBooking(booking);
    }

    public void saveAllEventBookings(List<EventBooking> bookings) {
        for (EventBooking booking : bookings) {
            fileStorageManager.saveEventBooking(booking);
        }
    }

    public boolean deleteEventBooking(Long bookingId) {
        return fileStorageManager.deleteEventBooking(bookingId);
    }

    public EventBooking updateEventBooking(EventBooking booking) {
        return fileStorageManager.updateEventBooking(booking);
    }

    // PurchasedVehicle operations
    public List<PurchasedVehicle> getAllPurchasedVehicles() {
        return fileStorageManager.loadAllPurchasedVehicles();
    }

    public PurchasedVehicle savePurchasedVehicle(PurchasedVehicle purchase) {
        return fileStorageManager.savePurchasedVehicle(purchase);
    }

    public void saveAllPurchasedVehicles(List<PurchasedVehicle> purchases) {
        for (PurchasedVehicle purchase : purchases) {
            fileStorageManager.savePurchasedVehicle(purchase);
        }
    }

    public boolean deletePurchasedVehicle(Long purchaseId) {
        return fileStorageManager.deletePurchasedVehicle(purchaseId);
    }

    public PurchasedVehicle updatePurchasedVehicle(PurchasedVehicle purchase) {
        return fileStorageManager.updatePurchasedVehicle(purchase);
    }

    // Payment operations
    public List<Payment> getAllPayments() {
        return fileStorageManager.loadAllPayments();
    }

    public Payment savePayment(Payment payment) {
        return fileStorageManager.savePayment(payment);
    }

    public boolean deletePayment(String paymentId) {
        return fileStorageManager.deletePayment(paymentId);
    }

    public Payment updatePayment(Payment payment) {
        return fileStorageManager.updatePayment(payment);
    }

    // Helper methods for finding specific entities
    public Optional<Customer> findCustomerById(int id) {
        return getAllCustomers().stream()
                .filter(c -> c.getCustomerId() == id)
                .findFirst();
    }

    public Optional<Vehicle> findVehicleById(String id) {
        return getAllVehicles().stream()
                .filter(v -> v.getVehicleID().equals(id))
                .findFirst();
    }

    public Optional<Event> findEventById(Long id) {
        return getAllEvents().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }

    public Optional<Discount> findDiscountById(Long id) {
        return getAllDiscounts().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst();
    }

    public Optional<Staff> findStaffById(Long id) {
        return getAllStaff().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    public Optional<EventBooking> findEventBookingById(Long id) {
        return getAllEventBookings().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst();
    }

    public Optional<EventBooking> findEventBookingByBookingId(String bookingId) {
        return getAllEventBookings().stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst();
    }

    public Optional<PurchasedVehicle> findPurchasedVehicleById(Long id) {
        return getAllPurchasedVehicles().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public Optional<PurchasedVehicle> findPurchasedVehicleByPurchaseId(String purchaseId) {
        return getAllPurchasedVehicles().stream()
                .filter(p -> p.getPurchaseId().equals(purchaseId))
                .findFirst();
    }

    // Additional methods for specific queries
    public List<EventBooking> findEventBookingsByCustomerId(int customerId) {
        return getAllEventBookings().stream()
                .filter(b -> b.getCustomer().getCustomerId() == customerId)
                .toList();
    }

    public List<PurchasedVehicle> findPurchasedVehiclesByCustomerId(int customerId) {
        return getAllPurchasedVehicles().stream()
                .filter(p -> p.getCustomer().getCustomerId() == customerId)
                .toList();
    }

    public List<Event> findEventsByType(String eventType) {
        return getAllEvents().stream()
                .filter(e -> e.getEventType().equals(eventType))
                .toList();
    }
}