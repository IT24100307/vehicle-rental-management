# Vehicle Rental System

This Spring Boot application provides a comprehensive solution for managing a vehicle rental service with event bookings.

## File-Based Storage

All data is persisted in text files instead of a database, with files stored in the "data" directory:

- `customers.txt` - Customer information
- `vehicles.txt` - Vehicle inventory
- `events.txt` - Available events
- `discounts.txt` - Discount information
- `staff.txt` - Staff information
- `event_bookings.txt` - Event booking records
- `purchased_vehicles.txt` - Vehicle rental records
- `payments.txt` - Payment records

## Vehicle Rental Process

1. When a customer rents a vehicle:

   - The vehicle is removed from the inventory
   - The vehicle is added to the customer's rented vehicles list
   - A payment is processed and recorded
   - The rental is saved as a PurchasedVehicle record

2. When a customer returns a vehicle:
   - The vehicle is added back to the inventory
   - The vehicle is removed from the customer's rented vehicles list
   - The PurchasedVehicle record is deleted

## Event Booking Process

1. Customers can browse available events
2. When a customer books an event:
   - A new EventBooking record is created
   - Payment can be processed ("buy" option)
   - The booking is confirmed once payment is completed

## Payment System

Both cash and card payments are supported:

- Vehicle rentals can be paid for using either method
- Event bookings can be paid for using either method

## Running the Application

```
./mvnw.cmd spring-boot:run
```

The application will start with default entities loaded from files or, if files don't exist yet, from the in-memory inventory.

## Key Features

- Customer registration and login
- Vehicle management
- Event management
- Booking system
- Discount management
- Staff management

## Data Persistence

When a user logs in or creates entities like vehicles, events, discounts, or staff members, the data is:

1. Parsed from the appropriate text files
2. Modified in memory
3. Written back to the text files

This approach eliminates the need for a database server while maintaining data persistence.

## File Storage Implementation

### FileStorageManager

The `FileStorageManager` class (`src/main/java/com/system/project1/util/FileStorageManager.java`) handles all file operations:

- Reading from files
- Writing to files
- Converting between entity objects and text file formats

### FileStorageService

The `FileStorageService` class (`src/main/java/com/system/project1/service/FileStorageService.java`) provides service-level methods for the application to interact with the file-based storage system.

## Security

Note that customer passwords are stored in plain text in the files. In a production environment, proper encryption and security measures should be implemented.
