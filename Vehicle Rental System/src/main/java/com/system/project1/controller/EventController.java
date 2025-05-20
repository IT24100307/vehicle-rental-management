package com.system.project1.controller;

import com.system.project1.entity.Event;
import com.system.project1.entity.Vehicle;
import com.system.project1.service.EventService;
import com.system.project1.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private VehicleService vehicleService;

    @Value("${file.upload-dir:src/main/resources/static/images/events}")
    private String uploadDir;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // Get all events
    @GetMapping("/events")
    public ResponseEntity<List<Event>> getAllEvents() {
        // Return an empty list if there are no events
        List<Event> events = eventService.getAllEvents();
        // Make sure we never return null, always return an empty list at minimum
        if (events == null) {
            events = new ArrayList<>();
        }
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    // Get event by id
    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        if (event != null) {
            return new ResponseEntity<>(event, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Create new event
    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        try {
            System.out.println("Received event data: " + event);

            // Handle dates if they come as strings
            if (event.getStartDate() == null && event.getStartDateStr() != null) {
                try {
                    event.setStartDate(DATE_FORMAT.parse(event.getStartDateStr()));
                } catch (ParseException e) {
                    System.out.println("Error parsing start date: " + e.getMessage());
                    return ResponseEntity.badRequest().body("Invalid start date format: " + event.getStartDateStr());
                }
            }

            if (event.getEndDate() == null && event.getEndDateStr() != null) {
                try {
                    event.setEndDate(DATE_FORMAT.parse(event.getEndDateStr()));
                } catch (ParseException e) {
                    System.out.println("Error parsing end date: " + e.getMessage());
                    return ResponseEntity.badRequest().body("Invalid end date format: " + event.getEndDateStr());
                }
            }

            // Validate required fields
            if (event.getName() == null || event.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Event name is required");
            }

            if (event.getStartDate() == null) {
                return ResponseEntity.badRequest().body("Start date is required");
            }

            if (event.getEndDate() == null) {
                return ResponseEntity.badRequest().body("End date is required");
            }

            if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Event type is required");
            }

            if (event.getPrice() <= 0) {
                return ResponseEntity.badRequest().body("Event price must be greater than zero");
            }

            // Check if there are any vehicle IDs
            if (event.getVehicleIds() == null || event.getVehicleIds().isEmpty()) {
                System.out.println("Error: No vehicles selected for event");
                return ResponseEntity.badRequest().body("At least one vehicle must be selected for the event");
            }

            // Process vehicle IDs if provided
            if (event.getVehicleIds() != null && !event.getVehicleIds().isEmpty()) {
                System.out.println("Processing vehicle IDs: " + event.getVehicleIds());

                // Validate that all vehicle IDs exist
                List<String> invalidVehicleIds = new ArrayList<>();
                for (String vehicleId : event.getVehicleIds()) {
                    if (vehicleService.findVehicleById(vehicleId) == null) {
                        invalidVehicleIds.add(vehicleId);
                    }
                }

                if (!invalidVehicleIds.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body("The following vehicle IDs are invalid: " + String.join(", ", invalidVehicleIds));
                }
            }

            try {
                Event createdEvent = eventService.saveEvent(event);
                return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
            } catch (RuntimeException e) {
                // Specific handling for database errors
                System.out.println("Database error when saving event: " + e.getMessage());
                Throwable cause = e.getCause();
                if (cause != null) {
                    System.out.println("Cause: " + cause.getMessage());
                    cause.printStackTrace();
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Unable to save event to database: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            System.out.println("Validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error creating event: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating event: " + e.getMessage());
        }
    }

    // Update event
    @PutMapping("/events/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event event) {
        try {
            Event existingEvent = eventService.getEventById(id);
            if (existingEvent == null) {
                return new ResponseEntity<>("Event not found with ID: " + id, HttpStatus.NOT_FOUND);
            }

            System.out.println("Updating event with ID: " + id);
            System.out.println("Received event data: " + event);

            event.setId(id);

            // Handle dates if they come as strings
            if (event.getStartDate() == null && event.getStartDateStr() != null) {
                try {
                    event.setStartDate(DATE_FORMAT.parse(event.getStartDateStr()));
                } catch (ParseException e) {
                    System.out.println("Error parsing start date: " + e.getMessage());
                    return ResponseEntity.badRequest()
                            .body("Invalid start date format: " + event.getStartDateStr());
                }
            }

            if (event.getEndDate() == null && event.getEndDateStr() != null) {
                try {
                    event.setEndDate(DATE_FORMAT.parse(event.getEndDateStr()));
                } catch (ParseException e) {
                    System.out.println("Error parsing end date: " + e.getMessage());
                    return ResponseEntity.badRequest().body("Invalid end date format: " + event.getEndDateStr());
                }
            }

            // Validate required fields
            if (event.getName() == null || event.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Event name is required");
            }

            if (event.getStartDate() == null) {
                return ResponseEntity.badRequest().body("Start date is required");
            }

            if (event.getEndDate() == null) {
                return ResponseEntity.badRequest().body("End date is required");
            }

            if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Event type is required");
            }

            if (event.getPrice() <= 0) {
                return ResponseEntity.badRequest().body("Event price must be greater than zero");
            }

            // Check if there are any vehicle IDs
            if (event.getVehicleIds() == null || event.getVehicleIds().isEmpty()) {
                return ResponseEntity.badRequest().body("At least one vehicle must be selected for the event");
            }

            // Process vehicle IDs if provided
            if (event.getVehicleIds() != null && !event.getVehicleIds().isEmpty()) {
                System.out.println("Processing vehicle IDs for update: " + event.getVehicleIds());

                // Validate that all vehicle IDs exist
                List<String> invalidVehicleIds = new ArrayList<>();
                for (String vehicleId : event.getVehicleIds()) {
                    if (vehicleService.findVehicleById(vehicleId) == null) {
                        invalidVehicleIds.add(vehicleId);
                    }
                }

                if (!invalidVehicleIds.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body("The following vehicle IDs are invalid: " + String.join(", ", invalidVehicleIds));
                }
            }

            try {
                Event updatedEvent = eventService.saveEvent(event);
                return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
            } catch (RuntimeException e) {
                // Specific handling for database errors
                System.out.println("Database error when updating event: " + e.getMessage());
                Throwable cause = e.getCause();
                if (cause != null) {
                    System.out.println("Cause: " + cause.getMessage());
                    cause.printStackTrace();
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Unable to update event in database: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            System.out.println("Validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error updating event: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating event: " + e.getMessage());
        }
    }

    // Delete event
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        Event existingEvent = eventService.getEventById(id);
        if (existingEvent != null) {
            // This will delete the event from both memory and file storage
            eventService.deleteEvent(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Get vehicles for an event
    @GetMapping("/events/{id}/vehicles")
    public ResponseEntity<List<Vehicle>> getEventVehicles(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        if (event != null) {
            return new ResponseEntity<>(event.getVehicles(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Get all available vehicles for events
    @GetMapping("/events/available-vehicles")
    public ResponseEntity<List<Vehicle>> getAvailableVehiclesForEvents() {
        // Clear any cached data first
        System.out.println("Fetching available vehicles for event creation at " + new Date());

        List<Vehicle> vehicles = vehicleService.getAvailableVehicles();
        // Make sure we never return null, always return an empty list at minimum
        if (vehicles == null) {
            vehicles = new ArrayList<>();
        }

        // Log the count to help with debugging
        System.out.println("Returning " + vehicles.size() + " available vehicles for event creation");

        // Set no-cache headers in the response
        return ResponseEntity
                .ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(vehicles);
    }

    // Upload event image
    @PostMapping("/upload/event-image")
    public ResponseEntity<?> uploadEventImage(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                System.out.println("Upload failed: File is empty");
                return ResponseEntity.badRequest().body(new UploadResponse(null, "File is empty"));
            }

            // Log request details for debugging
            System.out.println("Received upload request for file: " + file.getOriginalFilename()
                    + ", size: " + file.getSize() + " bytes");

            // Ensure absolute path for the upload directory
            File uploadsDir = new File(uploadDir);
            if (!uploadsDir.exists()) {
                System.out.println("Creating upload directory: " + uploadsDir.getAbsolutePath());
                boolean dirCreated = uploadsDir.mkdirs();
                if (!dirCreated) {
                    System.out.println("Failed to create upload directory. Trying absolute path...");
                    // Try with an absolute path
                    String absolutePath = new File("").getAbsolutePath() + File.separator + uploadDir;
                    uploadsDir = new File(absolutePath);
                    if (!uploadsDir.exists()) {
                        boolean absPathCreated = uploadsDir.mkdirs();
                        if (!absPathCreated) {
                            throw new IOException("Failed to create upload directory: " + absolutePath);
                        }
                        uploadDir = absolutePath;
                    }
                }
            }

            System.out.println("Using upload directory: " + uploadsDir.getAbsolutePath());

            // Generate a unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename);

            System.out.println("Saving file to: " + filePath.toString());

            // Save the file
            Files.write(filePath, file.getBytes());

            // Return the path that can be used in the image tag src attribute
            String imageUrl = "/images/events/" + filename;
            System.out.println("File uploaded successfully. Image URL: " + imageUrl);

            return ResponseEntity.ok().body(new UploadResponse(imageUrl, null));
        } catch (Exception e) {
            System.out.println("Error during file upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UploadResponse(null, "Failed to upload image: " + e.getMessage()));
        }
    }

    // Inner class for image upload response
    private static class UploadResponse {
        private String imagePath;
        private String error;

        public UploadResponse(String imagePath, String error) {
            this.imagePath = imagePath;
            this.error = error;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
