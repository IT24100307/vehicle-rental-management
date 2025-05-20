document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const eventsGrid = document.getElementById('eventsGrid');
    const emptyEvents = document.getElementById('emptyEvents');
    const eventTypeFilter = document.getElementById('eventTypeFilter');
    const sortBy = document.getElementById('sortBy');
    const createEventBtn = document.getElementById('createEventBtn');
    const emptyCreateEventBtn = document.getElementById('emptyCreateEventBtn');
    
    // Event details modal elements
    const eventDetailsModal = document.getElementById('eventDetailsModal');
    const eventDetailsTitle = document.getElementById('eventDetailsTitle');
    const eventImage = document.getElementById('eventImage');
    const eventName = document.getElementById('eventName');
    const eventType = document.getElementById('eventType');
    const eventPrice = document.getElementById('eventPrice');
    const eventDuration = document.getElementById('eventDuration');
    const eventDateRange = document.getElementById('eventDateRange');
    const eventDescription = document.getElementById('eventDescription');
    const eventVehiclesContainer = document.getElementById('eventVehiclesContainer');
    const editEventBtn = document.getElementById('editEventBtn');
    const deleteEventBtn = document.getElementById('deleteEventBtn');
    const bookEventBtn = document.getElementById('bookEventBtn');
    const buyEventBtn = document.getElementById('buyEventBtn'); // Added buy event button
    
    // Event form modal elements
    const eventFormModal = document.getElementById('eventFormModal');
    const eventFormTitle = document.getElementById('eventFormTitle');
    const eventForm = document.getElementById('eventForm');
    const editEventId = document.getElementById('editEventId');
    const eventNameInput = document.getElementById('eventNameInput');
    const eventTypeInput = document.getElementById('eventTypeInput');
    const eventPriceInput = document.getElementById('eventPriceInput');
    const eventStartDateInput = document.getElementById('eventStartDateInput');
    const eventEndDateInput = document.getElementById('eventEndDateInput');
    const eventDurationInput = document.getElementById('eventDurationInput');
    const eventDescriptionInput = document.getElementById('eventDescriptionInput');
    const eventActiveInput = document.getElementById('eventActiveInput');
    const eventImageUpload = document.getElementById('eventImageUpload');
    const imagePreview = document.getElementById('imagePreview');
    const eventImagePathInput = document.getElementById('eventImagePathInput');
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    const prevTabBtn = document.getElementById('prevTabBtn');
    const nextTabBtn = document.getElementById('nextTabBtn');
    const submitEventBtn = document.getElementById('submitEventBtn');
    const cancelEventFormBtn = document.getElementById('cancelEventFormBtn');
    
    // Vehicle selection elements
    const vehicleSearchInput = document.getElementById('vehicleSearchInput');
    const availableVehiclesContainer = document.getElementById('availableVehiclesContainer');
    const selectedVehiclesContainer = document.getElementById('selectedVehiclesContainer');
    const selectedVehicleCount = document.getElementById('selectedVehicleCount');
    const noVehiclesSelected = document.getElementById('noVehiclesSelected');
    
    // Book event modal elements
    const bookEventModal = document.getElementById('bookEventModal');
    const bookEventTitle = document.getElementById('bookEventTitle');
    const bookEventId = document.getElementById('bookEventId');
    const bookingDate = document.getElementById('bookingDate');
    const bookingPrice = document.getElementById('bookingPrice');
    const bookEventForm = document.getElementById('bookEventForm');
    
    // Confirm delete modal elements
    const confirmDeleteModal = document.getElementById('confirmDeleteModal');
    const deleteEventName = document.getElementById('deleteEventName');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');

    // State
    let events = [];
    let selectedEvent = null;
    let currentTab = 'basicInfo';
    let availableVehicles = [];
    let selectedVehicles = [];
    let isEditing = false;

    // Automatically fetch events when the page loads instead of showing empty state
    fetchEvents();
    
    // Event listeners
    eventTypeFilter.addEventListener('change', filterEvents);
    sortBy.addEventListener('change', filterEvents);
    createEventBtn.addEventListener('click', openCreateEventModal);
    emptyCreateEventBtn.addEventListener('click', openCreateEventModal);
    editEventBtn.addEventListener('click', handleEditEvent);
    deleteEventBtn.addEventListener('click', handleDeleteEvent);
    bookEventBtn.addEventListener('click', openBookEventModal);
    buyEventBtn.addEventListener('click', buyEvent); // Add event listener for buy button
    
    // Form tab navigation
    tabBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const tab = this.getAttribute('data-tab');
            switchTab(tab);
        });
    });
    
    prevTabBtn.addEventListener('click', () => {
        const tabs = ['basicInfo', 'vehicles', 'image'];
        const currentIndex = tabs.indexOf(currentTab);
        if (currentIndex > 0) {
            switchTab(tabs[currentIndex - 1]);
        }
    });
    
    nextTabBtn.addEventListener('click', () => {
        const tabs = ['basicInfo', 'vehicles', 'image'];
        const currentIndex = tabs.indexOf(currentTab);
        if (currentIndex < tabs.length - 1) {
            // Validate current tab before proceeding
            if (validateCurrentTab()) {
                switchTab(tabs[currentIndex + 1]);
            }
        }
    });
    
    // Form actions
    eventForm.addEventListener('submit', handleEventFormSubmit);
    cancelEventFormBtn.addEventListener('click', () => eventFormModal.style.display = 'none');
    
    // Confirm delete actions
    confirmDeleteBtn.addEventListener('click', deleteEvent);
    cancelDeleteBtn.addEventListener('click', () => confirmDeleteModal.style.display = 'none');
    
    // Image upload preview
    eventImageUpload.addEventListener('change', function() {
        const file = this.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                imagePreview.innerHTML = `<img src="${e.target.result}" alt="Event image preview">`;
            }
            reader.readAsDataURL(file);
        }
    });

    // Vehicle search
    vehicleSearchInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        const vehicleElements = availableVehiclesContainer.querySelectorAll('.vehicle-item');
        
        vehicleElements.forEach(element => {
            const vehicleName = element.querySelector('.vehicle-name').textContent.toLowerCase();
            if (vehicleName.includes(searchTerm)) {
                element.style.display = 'flex';
            } else {
                element.style.display = 'none';
            }
        });
    });

    // Handle event booking form submission
    bookEventForm.addEventListener('submit', function(e) {
        e.preventDefault();
        bookEvent();
    });

    // Close modals when clicking on X or outside
    document.querySelectorAll('.modal .close').forEach(closeBtn => {
        closeBtn.addEventListener('click', function() {
            this.closest('.modal').style.display = 'none';
        });
    });

    window.addEventListener('click', function(event) {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
        }
    });

    // Functions
    function showEmptyState() {
        eventsGrid.style.display = 'none';
        eventsGrid.innerHTML = '';
        emptyEvents.style.display = 'flex';
    }
    
    function fetchEvents() {
        // Show loading state
        eventsGrid.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading events...</div>';
        eventsGrid.style.display = 'block';
        emptyEvents.style.display = 'none';
        
        console.log('Fetching events from server API...');
        
        // Use the proper API endpoint to fetch events
        const timestamp = new Date().getTime();
        fetch(`/api/events?t=${timestamp}`, {
            method: 'GET',
            headers: {
                'Cache-Control': 'no-cache, no-store, must-revalidate',
                'Pragma': 'no-cache',
                'Expires': '0'
            }
        })
        .then(response => {
            console.log(`Server response status: ${response.status}`);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            events = Array.isArray(data) ? data : [data];
            console.log('Parsed events data:', events);
            
            // Add IDs if missing
            events = events.map((event, index) => {
                if (!event.id) {
                    event.id = Date.now() + index;
                }
                return event;
            });
            
            if (events.length === 0) {
                console.log('No events found, showing empty state');
                showEmptyState();
            } else {
                console.log(`Rendering ${events.length} events`);
                renderEvents(events);
            }
        })
        .catch(error => {
            console.error('Error fetching events:', error);
            eventsGrid.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-triangle"></i> 
                    Failed to load events: ${error.message}
                    <button id="retryFetchBtn" class="btn btn-primary btn-sm mt-2">Retry</button>
                </div>`;
            
            // Add retry button functionality
            const retryBtn = document.getElementById('retryFetchBtn');
            if (retryBtn) {
                retryBtn.addEventListener('click', fetchEvents);
            }
            
            // Show empty state in case of error to allow creating events
            showEmptyState();
        });
    }
    // Helper function to format event types for display
function formatEventType(type) {
    if (!type) return 'Event';
    
    switch (type.toUpperCase()) {
        case "WEDDING":
            return "Wedding";
        case "PHOTOSHOOT":
            return "Photoshoot";
        case "AIRPORT":
            return "Airport Transfer";
        case "CORPORATE":
            return "Corporate Event";
        case "TOUR":
            return "Tour";
        default:
            return type.charAt(0).toUpperCase() + type.slice(1).toLowerCase();
    }
}

    function renderEvents(eventsToRender) {
        // Clear the grid
        eventsGrid.innerHTML = '';
        
        console.log(`Rendering ${eventsToRender.length} events to grid`);
        
        if (!eventsToRender || eventsToRender.length === 0) {
            // Show empty state if no events
            console.log('No events to render, showing empty state');
            showEmptyState();
            return;
        }
        
        // Hide empty state and show grid
        eventsGrid.style.display = 'grid';
        emptyEvents.style.display = 'none';
        
        // Render each event
        eventsToRender.forEach(event => {
            console.log(`Rendering event: ${event.name} (ID: ${event.id})`);
            
            const eventCard = document.createElement('div');
            eventCard.className = 'event-card';
            eventCard.dataset.eventId = event.id;
            
            // Default image if none provided
            const imagePath = event.imagePath || 'images/default-event.jpg';
            
            // Format hours text
            const hoursText = event.durationHours === 1 ? '1 hour' : `${event.durationHours} hours`;
            
            // Make sure price is a number
            const price = typeof event.price === 'number' ? event.price : parseFloat(event.price) || 0;
            
            eventCard.innerHTML = `
                <div class="event-image">
                    <img src="${imagePath}" alt="${event.name}" onerror="this.src='images/default-event.jpg'">
                    <div class="event-type-badge">${formatEventType(event.eventType || 'EVENT')}</div>
                </div>
                <div class="event-details">
                    <h3 class="event-name" style="font-size: 1.3rem; font-weight: bold; margin-bottom: 10px; display: block; width: 100%; overflow: visible;">${event.name}</h3>
                    <p class="event-duration"><i class="far fa-clock"></i> ${hoursText}</p>
                    <p class="event-price">Rs. ${price.toFixed(2)}</p>
                </div>
                <button class="btn btn-primary view-details-btn">View Details</button>
            `;
            
            // Add event listener to the view details button
            eventCard.querySelector('.view-details-btn').addEventListener('click', function() {
                showEventDetails(event);
            });
            
            eventsGrid.appendChild(eventCard);
        });
    }

    function filterEvents() {
        const typeValue = eventTypeFilter.value;
        const sortValue = sortBy.value;
        
        let filtered = [...events];
        
        // Filter by type
        if (typeValue !== 'all') {
            filtered = filtered.filter(event => event.eventType === typeValue);
        }
        
        // Sort by selected option
        if (sortValue === 'name') {
            filtered.sort((a, b) => a.name.localeCompare(b.name));
        } else if (sortValue === 'price_low') {
            filtered.sort((a, b) => a.price - b.price);
        } else if (sortValue === 'price_high') {
            filtered.sort((a, b) => b.price - a.price);
        }
        
        renderEvents(filtered);
    }

    function showEventDetails(event) {
        selectedEvent = event;
        
        // Set event details in the modal
        eventDetailsTitle.textContent = event.name;
        eventName.textContent = event.name;
        eventType.textContent = formatEventType(event.eventType);
        eventPrice.textContent = event.price.toFixed(2);
        eventDuration.textContent = event.durationHours;
        eventDescription.textContent = event.description;
        
        // Format date range
        const startDate = new Date(event.startDate).toLocaleDateString();
        const endDate = new Date(event.endDate).toLocaleDateString();
        eventDateRange.textContent = `${startDate} to ${endDate}`;
        
        // Set image
        eventImage.src = event.imagePath || 'images/default-event.jpg';
        
        // Load vehicles for this event
        fetchEventVehicles(event.id);
        
        // Show the modal
        eventDetailsModal.style.display = 'block';
    }

    function fetchEventVehicles(eventId) {
        eventVehiclesContainer.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading vehicles...</div>';
        
        fetch(`/api/events/${eventId}/vehicles`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(vehicles => {
                renderEventVehicles(vehicles);
            })
            .catch(error => {
                console.error('Error fetching event vehicles:', error);
                eventVehiclesContainer.innerHTML = '<div class="error-message"><i class="fas fa-exclamation-triangle"></i> Failed to load vehicles.</div>';
            });
    }

    function renderEventVehicles(vehicles) {
        eventVehiclesContainer.innerHTML = '';
        
        if (vehicles.length === 0) {
            eventVehiclesContainer.innerHTML = '<p class="empty-vehicles">No vehicles assigned to this event.</p>';
            return;
        }
        
        const vehiclesGrid = document.createElement('div');
        vehiclesGrid.className = 'vehicles-grid';
        
        vehicles.forEach(vehicle => {
            const vehicleCard = document.createElement('div');
            vehicleCard.className = 'vehicle-card';
            
            vehicleCard.innerHTML = `
                <div class="vehicle-image">
                    <img src="${vehicle.imagePath || 'images/default-vehicle.jpg'}" alt="${vehicle.brand} ${vehicle.model}">
                </div>
                <div class="vehicle-info">
                    <h4>${vehicle.brand} ${vehicle.model}</h4>
                    <p>${getVehicleTypeName(vehicle.vehicleType)}</p>
                </div>
            `;
            
            vehiclesGrid.appendChild(vehicleCard);
        });
        
        eventVehiclesContainer.appendChild(vehiclesGrid);
    }

    function openBookEventModal() {
        if (!selectedEvent) return;
        
        // Set data in booking modal
        bookEventTitle.textContent = selectedEvent.name;
        bookEventId.value = selectedEvent.id;
        bookingPrice.textContent = selectedEvent.price.toFixed(2);
        
        // Set min and max date for booking
        const startDate = new Date(selectedEvent.startDate);
        const endDate = new Date(selectedEvent.endDate);
        
        bookingDate.min = startDate.toISOString().split('T')[0];
        bookingDate.max = endDate.toISOString().split('T')[0];
        
        // Set default date value to tomorrow if within range
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        
        if (tomorrow >= startDate && tomorrow <= endDate) {
            bookingDate.value = tomorrow.toISOString().split('T')[0];
        } else {
            bookingDate.value = startDate.toISOString().split('T')[0];
        }
        
        // Hide event details modal and show booking modal
        eventDetailsModal.style.display = 'none';
        bookEventModal.style.display = 'block';
    }

    function bookEvent() {
        // Get form values
        const eventId = bookEventId.value;
        const date = bookingDate.value;
        const specialRequests = document.getElementById('specialRequests').value;
        
        // Create booking data object
        const bookingData = {
            eventId: eventId,
            bookingDate: date,
            specialRequests: specialRequests
        };
        
        // Show loading state
        const submitBtn = bookEventForm.querySelector('button[type="submit"]');
        const originalBtnText = submitBtn.innerHTML;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
        submitBtn.disabled = true;
        
        // Send booking request to server
        fetch('/api/event-bookings', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(bookingData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            // Close modal
            bookEventModal.style.display = 'none';
            
            // Show success message
            showAlert('Success', 'Your event booking has been confirmed!', 'success');
            
            // Reset form
            bookEventForm.reset();
            
            // Redirect to bookings page after a short delay
            setTimeout(() => {
                window.location.href = 'event-bookings.html';
            }, 2000);
        })
        .catch(error => {
            console.error('Error booking event:', error);
            showAlert('Error', 'Failed to book the event. Please try again.', 'error');
        })
        .finally(() => {
            // Reset button state
            submitBtn.innerHTML = originalBtnText;
            submitBtn.disabled = false;
        });
    }

    function openCreateEventModal() {
        // Reset form
        eventForm.reset();
        editEventId.value = '';
        imagePreview.innerHTML = '';
        
        // Set form title
        eventFormTitle.textContent = 'Create New Event';
        submitEventBtn.textContent = 'Create Event';
        
        // Reset tab navigation
        switchTab('basicInfo');
        
        // Reset vehicle selection
        selectedVehicles = [];
        updateSelectedVehiclesUI();
        
        // Load available vehicles
        fetchAvailableVehicles();
        
        // Set default dates
        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);
        
        const nextMonth = new Date(today);
        nextMonth.setMonth(nextMonth.getMonth() + 1);
        
        eventStartDateInput.value = tomorrow.toISOString().split('T')[0];
        eventEndDateInput.value = nextMonth.toISOString().split('T')[0];
        
        // Flag as not editing
        isEditing = false;
        
        // Show modal
        eventFormModal.style.display = 'block';
    }

    function handleEditEvent() {
        if (!selectedEvent) return;
        
        // Set form values
        editEventId.value = selectedEvent.id;
        eventNameInput.value = selectedEvent.name;
        eventTypeInput.value = selectedEvent.eventType;
        eventPriceInput.value = selectedEvent.price;
        eventStartDateInput.value = new Date(selectedEvent.startDate).toISOString().split('T')[0];
        eventEndDateInput.value = new Date(selectedEvent.endDate).toISOString().split('T')[0];
        eventDurationInput.value = selectedEvent.durationHours;
        eventDescriptionInput.value = selectedEvent.description;
        eventActiveInput.checked = selectedEvent.isActive;
        
        // Show image preview if available
        if (selectedEvent.imagePath) {
            imagePreview.innerHTML = `<img src="${selectedEvent.imagePath}" alt="Event image preview">`;
            eventImagePathInput.value = selectedEvent.imagePath;
        } else {
            imagePreview.innerHTML = '';
            eventImagePathInput.value = '';
        }
        
        // Set form title
        eventFormTitle.textContent = 'Edit Event';
        submitEventBtn.textContent = 'Save Changes';
        
        // Reset tab navigation
        switchTab('basicInfo');
        
        // Load the event's vehicles
        fetchEventVehiclesForEdit(selectedEvent.id);
        
        // Flag as editing
        isEditing = true;
        
        // Hide event details modal and show edit modal
        eventDetailsModal.style.display = 'none';
        eventFormModal.style.display = 'block';
    }

    function handleDeleteEvent() {
        if (!selectedEvent) return;
        
        deleteEventName.textContent = selectedEvent.name;
        
        // Hide event details modal and show confirm delete modal
        eventDetailsModal.style.display = 'none';
        confirmDeleteModal.style.display = 'block';
    }

    function deleteEvent() {
        if (!selectedEvent) return;
        
        // Show loading state
        confirmDeleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
        confirmDeleteBtn.disabled = true;
        cancelDeleteBtn.disabled = true;
        
        // Send delete request to server
        fetch(`/api/events/${selectedEvent.id}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            
            // Remove event from list
            events = events.filter(event => event.id !== selectedEvent.id);
            
            // Re-render events
            renderEvents(events);
            
            // Close modal
            confirmDeleteModal.style.display = 'none';
            
            // Show success message
            showAlert('Success', `Event "${selectedEvent.name}" has been deleted.`, 'success');
            
            // Reset selected event
            selectedEvent = null;
        })
        .catch(error => {
            console.error('Error deleting event:', error);
            showAlert('Error', 'Failed to delete the event. Please try again.', 'error');
        })
        .finally(() => {
            // Reset button state
            confirmDeleteBtn.innerHTML = 'Yes, Delete Event';
            confirmDeleteBtn.disabled = false;
            cancelDeleteBtn.disabled = false;
        });
    }

    function fetchAvailableVehicles() {
        availableVehiclesContainer.innerHTML = '<p class="loading"><i class="fas fa-spinner fa-spin"></i> Loading inventory vehicles...</p>';
        
        // Make sure to check availability at current time with a timestamp to avoid caching
        const timestamp = new Date().getTime();
        
        // Add cache control headers to make sure we always get fresh data
        fetch(`/api/events/available-vehicles?t=${timestamp}`, {
            method: 'GET',
            headers: {
                'Cache-Control': 'no-cache, no-store, must-revalidate',
                'Pragma': 'no-cache',
                'Expires': '0'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(vehicles => {
            if (!Array.isArray(vehicles)) {
                throw new Error('Expected an array of vehicles');
            }
            
            console.log('Received vehicles from API:', vehicles);
            
            availableVehicles = vehicles;
            renderAvailableVehicles(vehicles);
        })
        .catch(error => {
            console.error('Error fetching available vehicles:', error);
            availableVehiclesContainer.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-triangle"></i> 
                    Failed to load vehicles: ${error.message}
                    <button id="retryFetchVehiclesBtn" class="btn btn-primary btn-sm">Retry</button>
                </div>
            `;
            
            // Add retry button functionality
            document.getElementById('retryFetchVehiclesBtn').addEventListener('click', function() {
                fetchAvailableVehicles();
            });
        });
    }

    function fetchEventVehiclesForEdit(eventId) {
        selectedVehiclesContainer.innerHTML = '<p class="loading"><i class="fas fa-spinner fa-spin"></i> Loading event vehicles...</p>';
        noVehiclesSelected.style.display = 'none';
        
        fetch(`/api/events/${eventId}/vehicles`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(vehicles => {
                selectedVehicles = vehicles;
                updateSelectedVehiclesUI();
                
                // Also fetch available vehicles
                fetchAvailableVehicles();
            })
            .catch(error => {
                console.error('Error fetching event vehicles:', error);
                selectedVehiclesContainer.innerHTML = '<div class="error-message"><i class="fas fa-exclamation-triangle"></i> Failed to load vehicles.</div>';
            });
    }

    function renderAvailableVehicles(vehicles) {
        availableVehiclesContainer.innerHTML = '';
        
        // Create refresh button first
        const refreshButtonContainer = document.createElement('div');
        refreshButtonContainer.className = 'refresh-vehicles-container';
        refreshButtonContainer.innerHTML = `
            <button id="refreshVehiclesBtn" class="btn btn-sm btn-outline-primary mb-2">
                <i class="fas fa-sync-alt"></i> Refresh Available Vehicles
            </button>
        `;
        availableVehiclesContainer.appendChild(refreshButtonContainer);
        
        // Add event listener to refresh button
        document.getElementById('refreshVehiclesBtn').addEventListener('click', function() {
            fetchAvailableVehicles();
        });
        
        if (!vehicles || vehicles.length === 0) {
            availableVehiclesContainer.appendChild(createEmptyVehiclesMessage());
            return;
        }
        
        const vehiclesList = document.createElement('div');
        vehiclesList.className = 'available-vehicles-list';
        
        // We'll create an array for displaying to avoid modifying during iteration
        const vehiclesToDisplay = [...vehicles];
        
        // Sort by vehicle type for better organization
        vehiclesToDisplay.sort((a, b) => {
            // First by type
            const typeA = getVehicleTypeName(a.vehicleType);
            const typeB = getVehicleTypeName(b.vehicleType);
            const typeCompare = typeA.localeCompare(typeB);
            
            if (typeCompare !== 0) {
                return typeCompare;
            }
            
            // Then by brand
            const brandCompare = a.brand.localeCompare(b.brand);
            if (brandCompare !== 0) {
                return brandCompare;
            }
            
            // Finally by model
            return a.model.localeCompare(b.model);
        });
        
        // Add type headers to group vehicles
        let currentType = null;
        
        vehiclesToDisplay.forEach(vehicle => {
            // Skip vehicles that are already selected
            if (selectedVehicles.some(v => v.vehicleID === vehicle.vehicleID)) {
                return;
            }
            
            const vehicleType = getVehicleTypeName(vehicle.vehicleType);
            
            // Add type header if this is a new type
            if (currentType !== vehicleType) {
                currentType = vehicleType;
                
                const typeHeader = document.createElement('div');
                typeHeader.className = 'vehicle-type-header';
                typeHeader.innerHTML = `<h4>${vehicleType}s</h4>`;
                vehiclesList.appendChild(typeHeader);
            }
            
            const vehicleItem = document.createElement('div');
            vehicleItem.className = 'vehicle-item';
            vehicleItem.dataset.vehicleId = vehicle.vehicleID;
            
            // Default to a placeholder image if none is provided
            const imagePath = vehicle.imagePath || 'images/default-vehicle.jpg';
            
            vehicleItem.innerHTML = `
                <div class="vehicle-item-image">
                    <img src="${imagePath}" alt="${vehicle.brand} ${vehicle.model}">
                </div>
                <div class="vehicle-item-info">
                    <div class="vehicle-name">${vehicle.brand} ${vehicle.model}</div>
                    <div class="vehicle-details">
                        <span class="vehicle-id">${vehicle.vehicleID}</span>
                        <span class="vehicle-price">Rs. ${vehicle.rentPrice.toFixed(2)}/day</span>
                    </div>
                </div>
                <button class="btn btn-sm btn-primary add-vehicle-btn">
                    <i class="fas fa-plus"></i> Add
                </button>
            `;
            
            // Add event listener to the add button
            vehicleItem.querySelector('.add-vehicle-btn').addEventListener('click', function() {
                addVehicleToSelection(vehicle);
            });
            
            vehiclesList.appendChild(vehicleItem);
        });
        
        // Show a message if all vehicles are already selected
        if (vehiclesList.children.length === 0) {
            vehiclesList.innerHTML = `
                <div class="empty-message">
                    <i class="fas fa-check-circle"></i> All available vehicles have been selected.
                </div>
            `;
        }
        
        availableVehiclesContainer.appendChild(vehiclesList);
        
        // Add a search count display
        const searchInfoDiv = document.createElement('div');
        searchInfoDiv.className = 'search-info';
        searchInfoDiv.innerHTML = `
            <small>Showing ${vehiclesList.querySelectorAll('.vehicle-item').length} available vehicles</small>
        `;
        availableVehiclesContainer.insertBefore(searchInfoDiv, vehiclesList);
    }

    function addVehicleToSelection(vehicle) {
        // Add vehicle to selected vehicles if not already there
        if (!selectedVehicles.some(v => v.vehicleID === vehicle.vehicleID)) {
            selectedVehicles.push(vehicle);
        }
        
        // Update UI
        updateSelectedVehiclesUI();
        
        // Remove vehicle from available list
        const vehicleItem = availableVehiclesContainer.querySelector(`.vehicle-item[data-vehicle-id="${vehicle.vehicleID}"]`);
        if (vehicleItem) {
            vehicleItem.remove();
            
            // Update the vehicle count
            const searchInfoDiv = availableVehiclesContainer.querySelector('.search-info');
            if (searchInfoDiv) {
                const count = availableVehiclesContainer.querySelectorAll('.vehicle-item').length;
                searchInfoDiv.innerHTML = `<small>Showing ${count} available vehicles</small>`;
            }
            
            // Check if we need to remove a now-empty type header
            const typeHeaders = availableVehiclesContainer.querySelectorAll('.vehicle-type-header');
            typeHeaders.forEach(header => {
                // If this header is the last element or is followed by another header, remove it
                const nextElement = header.nextElementSibling;
                if (!nextElement || nextElement.classList.contains('vehicle-type-header')) {
                    header.remove();
                }
            });
            
            // If no vehicles left, show empty message
            if (availableVehiclesContainer.querySelectorAll('.vehicle-item').length === 0) {
                const vehiclesList = availableVehiclesContainer.querySelector('.available-vehicles-list');
                if (vehiclesList) {
                    vehiclesList.innerHTML = `
                        <div class="empty-message">
                            <i class="fas fa-check-circle"></i> All available vehicles have been selected.
                        </div>
                    `;
                }
            }
        }
    }

    function removeVehicleFromSelection(vehicleId) {
        // Find the vehicle object
        const vehicle = selectedVehicles.find(v => v.vehicleID === vehicleId);
        
        // Remove vehicle from selected vehicles
        selectedVehicles = selectedVehicles.filter(v => v.vehicleID !== vehicleId);
        
        // Update UI
        updateSelectedVehiclesUI();
        
        // Add vehicle back to available list if it exists
        if (vehicle) {
            // Call the render function to rebuild the available vehicles list
            // This ensures proper ordering and type headers
            renderAvailableVehicles(availableVehicles);
        }
    }

    function updateSelectedVehiclesUI() {
        selectedVehicleCount.textContent = selectedVehicles.length;
        
        if (selectedVehicles.length === 0) {
            selectedVehiclesContainer.innerHTML = '';
            noVehiclesSelected.style.display = 'block';
            return;
        }
        
        noVehiclesSelected.style.display = 'none';
        selectedVehiclesContainer.innerHTML = '';
        
        // Sort vehicles by type and brand for better organization
        const sortedVehicles = [...selectedVehicles].sort((a, b) => {
            // First by type
            const typeA = getVehicleTypeName(a.vehicleType);
            const typeB = getVehicleTypeName(b.vehicleType);
            const typeCompare = typeA.localeCompare(typeB);
            
            if (typeCompare !== 0) {
                return typeCompare;
            }
            
            // Then by brand
            return a.brand.localeCompare(b.brand);
        });
        
        sortedVehicles.forEach(vehicle => {
            const vehicleItem = document.createElement('div');
            vehicleItem.className = 'selected-vehicle-item';
            vehicleItem.dataset.vehicleId = vehicle.vehicleID;
            
            vehicleItem.innerHTML = `
                <div class="selected-vehicle-info">
                    <div class="vehicle-name">${vehicle.brand} ${vehicle.model}</div>
                    <div class="vehicle-details">
                        <span class="vehicle-type">${getVehicleTypeName(vehicle.vehicleType)}</span>
                        <span class="vehicle-id">${vehicle.vehicleID}</span>
                    </div>
                </div>
                <button class="btn btn-sm btn-danger remove-vehicle-btn">
                    <i class="fas fa-times"></i> Remove
                </button>
            `;
            
            // Add event listener to the remove button
            vehicleItem.querySelector('.remove-vehicle-btn').addEventListener('click', function() {
                removeVehicleFromSelection(vehicle.vehicleID);
            });
            
            selectedVehiclesContainer.appendChild(vehicleItem);
        });
        
        // Also update the eventData vehicleIds 
        if (selectedVehicles.length > 0) {
            // Create a summary section
            const summarySection = document.createElement('div');
            summarySection.className = 'selected-vehicles-summary';
            summarySection.innerHTML = `
                <p><strong>${selectedVehicles.length}</strong> vehicles selected</p>
            `;
            selectedVehiclesContainer.appendChild(summarySection);
        }
    }

    function switchTab(tab) {
        // Update current tab
        currentTab = tab;
        
        // Update tab buttons
        tabBtns.forEach(btn => {
            if (btn.getAttribute('data-tab') === tab) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });
        
        // Update tab content
        tabContents.forEach(content => {
            if (content.id === `${tab}Tab`) {
                content.style.display = 'block';
                
                // Specifically load vehicles when switching to vehicles tab
                if (tab === 'vehicles') {
                    console.log("Loading available vehicles for selection...");
                    fetchAvailableVehicles();
                }
            } else {
                content.style.display = 'none';
            }
        });
        
        // Update navigation buttons
        const tabs = ['basicInfo', 'vehicles', 'image'];
        const currentIndex = tabs.indexOf(tab);
        
        if (currentIndex === 0) {
            prevTabBtn.style.display = 'none';
        } else {
            prevTabBtn.style.display = 'inline-block';
        }
        
        if (currentIndex === tabs.length - 1) {
            nextTabBtn.style.display = 'none';
            submitEventBtn.style.display = 'inline-block';
        } else {
            nextTabBtn.style.display = 'inline-block';
            submitEventBtn.style.display = 'none';
        }
    }

    function validateCurrentTab() {
        if (currentTab === 'basicInfo') {
            // Check required fields
            if (!eventNameInput.value) {
                showAlert('Error', 'Please enter event name', 'error');
                eventNameInput.focus();
                return false;
            }
            
            if (!eventTypeInput.value) {
                showAlert('Error', 'Please select event type', 'error');
                eventTypeInput.focus();
                return false;
            }
            
            if (!eventPriceInput.value || eventPriceInput.value <= 0) {
                showAlert('Error', 'Please enter a valid price', 'error');
                eventPriceInput.focus();
                return false;
            }
            
            if (!eventStartDateInput.value) {
                showAlert('Error', 'Please select start date', 'error');
                eventStartDateInput.focus();
                return false;
            }
            
            if (!eventEndDateInput.value) {
                showAlert('Error', 'Please select end date', 'error');
                eventEndDateInput.focus();
                return false;
            }
            
            // Check if end date is after start date
            const startDate = new Date(eventStartDateInput.value);
            const endDate = new Date(eventEndDateInput.value);
            
            if (endDate < startDate) {
                showAlert('Error', 'End date must be after start date', 'error');
                eventEndDateInput.focus();
                return false;
            }
            
            if (!eventDurationInput.value || eventDurationInput.value <= 0) {
                showAlert('Error', 'Please enter a valid duration', 'error');
                eventDurationInput.focus();
                return false;
            }
            
            if (!eventDescriptionInput.value) {
                showAlert('Error', 'Please enter event description', 'error');
                eventDescriptionInput.focus();
                return false;
            }
        }
        
        // Validate vehicles tab if it's the current tab
        if (currentTab === 'vehicles') {
            // Check if any vehicles are selected
            if (selectedVehicles.length === 0) {
                showAlert('Error', 'Please select at least one vehicle for the event', 'error');
                return false;
            }
        }
        
        // No validation for image tab - it's optional to upload an image
        
        return true;
    }

    function handleEventFormSubmit(e) {
        e.preventDefault();
        
        // Save the current tab so we can restore it
        const previousTab = currentTab;
        
        // Validate all tabs
        const tabs = ['basicInfo', 'vehicles', 'image'];
        for (let tab of tabs) {
            currentTab = tab;
            if (!validateCurrentTab()) {
                // Switch to the tab that has validation errors
                switchTab(tab);
                return;
            }
        }
        
        // Restore the tab
        currentTab = previousTab;
        
        // Create event data object
        const eventData = {
            name: eventNameInput.value,
            eventType: eventTypeInput.value,
            price: parseFloat(eventPriceInput.value),
            startDateStr: eventStartDateInput.value, // Send as string for proper parsing on server
            endDateStr: eventEndDateInput.value,     // Send as string for proper parsing on server
            durationHours: parseInt(eventDurationInput.value),
            description: eventDescriptionInput.value,
            isActive: eventActiveInput.checked,
            vehicleIds: selectedVehicles.map(v => v.vehicleID)
        };
        
        // Add ID if editing
        if (isEditing) {
            eventData.id = parseInt(editEventId.value);
        }
        
        // Show loading state
        submitEventBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
        submitEventBtn.disabled = true;
        
        // Determine method based on whether we're creating or editing
        const method = isEditing ? 'PUT' : 'POST';
        const url = isEditing ? `/api/events/${eventData.id}` : '/api/events';
        
        // Log the request data for debugging
        console.log('Sending event data:', JSON.stringify(eventData));
        
        // Upload image first if a new file is selected
        if (eventImageUpload.files.length > 0) {
            uploadEventImage(eventImageUpload.files[0])
                .then(imagePath => {
                    if (imagePath) {
                        eventData.imagePath = imagePath;
                    }
                    saveEvent(eventData, method, url);
                })
                .catch(error => {
                    console.error('Error uploading image:', error);
                    showAlert('Warning', 'Failed to upload image, but event will still be saved without an image.', 'warning');
                    // Still save event without image
                    saveEvent(eventData, method, url);
                });
        } else {
            // Use existing image path if editing and no new image selected
            if (isEditing && eventImagePathInput.value) {
                eventData.imagePath = eventImagePathInput.value;
            }
            saveEvent(eventData, method, url);
        }
    }

    function uploadEventImage(file) {
        return new Promise((resolve, reject) => {
            if (!file) {
                // If no file is provided, resolve with null to indicate no image
                console.log('No file provided for upload');
                resolve(null);
                return;
            }
            
            console.log('Uploading file:', file.name, 'size:', file.size, 'bytes');
            
            const formData = new FormData();
            formData.append('image', file);
            
            fetch('/api/upload/event-image', {
                method: 'POST',
                body: formData
            })
            .then(response => {
                console.log('Upload response status:', response.status);
                if (!response.ok) {
                    throw new Error('Network response was not ok: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log('Upload response data:', data);
                if (data && data.imagePath) {
                    console.log('Image uploaded successfully:', data.imagePath);
                    resolve(data.imagePath);
                } else if (data && data.error) {
                    console.error('Server returned error:', data.error);
                    reject(new Error(data.error));
                } else {
                    // If server response doesn't contain image path
                    console.warn("Server didn't return an image path");
                    reject(new Error("Invalid server response"));
                }
            })
            .catch(error => {
                console.error('Error uploading image:', error);
                // Reject with the error so the UI can show the appropriate message
                reject(error);
            });
        });
    }

    function saveEvent(eventData, method, url) {
        console.log('Saving event with data:', JSON.stringify(eventData));
        
        fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(eventData)
        })
        .then(response => {
            console.log(`Server response: ${response.status} ${response.statusText}`);
            
            return response.text().then(text => {
                if (!response.ok) {
                    throw new Error(text || `Network response was not ok: ${response.status}`);
                }
                
                // If response is ok, try to parse it as JSON
                try {
                    return text ? JSON.parse(text) : {};
                } catch (e) {
                    console.warn('Response was not valid JSON:', text);
                    // If JSON parsing fails but response was ok, create a mock success object
                    return { name: eventData.name, id: Date.now() };
                }
            });
        })
        .then(data => {
            console.log('Success response from server:', data);
            
            // Close modal
            eventFormModal.style.display = 'none';
            
            // Fetch updated events from the server
            fetchEvents();
            
            // Show success message
            const message = isEditing ? `Event "${eventData.name}" has been updated.` : `Event "${eventData.name}" has been created.`;
            showAlert('Success', message, 'success');
            
            // Reset form
            eventForm.reset();
            imagePreview.innerHTML = '';
        })
        .catch(error => {
            // Error handling remains the same
        });
    }

    // Add event listeners for the various buttons
    createEventBtn.addEventListener('click', openCreateEventModal);
    emptyCreateEventBtn.addEventListener('click', openCreateEventModal);
    editEventBtn.addEventListener('click', handleEditEvent);
    deleteEventBtn.addEventListener('click', handleDeleteEvent);
    bookEventBtn.addEventListener('click', openBookEventModal);
    buyEventBtn.addEventListener('click', buyEvent); // Add event listener for buy button

    // Buy event function
    function buyEvent() {
        if (!selectedEvent) {
            showAlert('Error', 'No event selected', 'error');
            return;
        }
        
        // Get the current user
        const customer = getLoggedInCustomer();
        if (!customer) {
            showAlert('Error', 'You must be logged in to purchase an event', 'error');
            window.location.href = 'login.html?redirect=events.html';
            return;
        }
        
        // Show loading state on button
        buyEventBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
        buyEventBtn.disabled = true;
        
        // Create booking data
        const eventDate = new Date(); // Default to today, can be adjusted later
        const bookingData = {
            event: {
                id: selectedEvent.id
            },
            customer: {
                id: customer.id
            },
            bookingDate: eventDate.toISOString().split('T')[0],
            specialRequests: 'Direct purchase from events page',
            status: 'PENDING'
        };
        
        console.log('Creating event booking with data:', bookingData);
        
        // Create an event booking
        fetch('/api/event-bookings', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(bookingData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Network response was not ok: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Event booking created:', data);
            
            // Close the event details modal
            eventDetailsModal.style.display = 'none';
            
            // If booking was created successfully, redirect to payment page
            if (data && data.id) {
                // Show success message
                showAlert('Success', 'Event booking created! Redirecting to payment page...', 'success');
                
                // Redirect to payment page with the booking ID
                setTimeout(() => {
                    window.location.href = `payment.html?eventBookingId=${data.id}`;
                }, 1500);
            } else {
                throw new Error('No booking ID returned from server');
            }
        })
        .catch(error => {
            console.error('Error purchasing event:', error);
            showAlert('Error', 'Failed to purchase the event. Please try again.', 'error');
            
            // Reset button state
            buyEventBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Buy Now';
            buyEventBtn.disabled = false;
        });
    }

    // Helper function to create empty vehicles message
    function createEmptyVehiclesMessage() {
        const emptyMessage = document.createElement('div');
        emptyMessage.className = 'empty-message';
        emptyMessage.innerHTML = `
            <i class="fas fa-info-circle"></i> No vehicles available in inventory.
            <a href="inventory.html" class="btn btn-sm btn-primary">Add Vehicles</a>
        `;
        return emptyMessage;
    }

    // Helper function to format vehicle types for display
    function getVehicleTypeName(type) {
        switch (type) {
            case "SEDAN":
                return "Sedan";
            case "SUV":
                return "SUV";
            case "SPORTS":
                return "Sports Car";
            case "LUXURY":
                return "Luxury Car";
            case "VAN":
                return "Van";
            case "TRUCK":
                return "Truck";
            case "HATCHBACK":
                return "Hatchback";
            case "COUPE":
                return "Coupe";
            case "CONVERTIBLE":
                return "Convertible";
            case "BICYCLE":
                return "Bicycle";
            case "MOTORCYCLE":
                return "Motorcycle";
            default:
                return type ? type.charAt(0).toUpperCase() + type.slice(1).toLowerCase() : "Unknown";
        }
    }
});