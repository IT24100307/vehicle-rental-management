// Admin Events Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const eventsTableBody = document.getElementById('eventsTableBody');
    const emptyEvents = document.getElementById('emptyEvents');
    const eventTypeFilter = document.getElementById('eventTypeFilter');
    const statusFilter = document.getElementById('statusFilter');
    const addEventBtn = document.getElementById('addEventBtn');
    
    // Modals
    const eventFormModal = document.getElementById('eventFormModal');
    const eventFormTitle = document.getElementById('eventFormTitle');
    const eventForm = document.getElementById('eventForm');
    const eventFormClose = eventFormModal.querySelector('.close');
    const cancelEventBtn = document.getElementById('cancelEventBtn');
    
    const manageVehiclesModal = document.getElementById('manageVehiclesModal');
    const manageVehiclesEventName = document.getElementById('manageVehiclesEventName');
    const currentVehiclesList = document.getElementById('currentVehiclesList');
    const currentVehicleCount = document.getElementById('currentVehicleCount');
    const availableVehiclesList = document.getElementById('availableVehiclesList');
    const vehicleSearchInput = document.getElementById('vehicleSearchInput');
    const manageVehiclesClose = manageVehiclesModal.querySelector('.close');
    
    const confirmDeleteModal = document.getElementById('confirmDeleteModal');
    const deleteEventName = document.getElementById('deleteEventName');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
    const confirmDeleteClose = confirmDeleteModal.querySelector('.close');
    
    // Form elements
    const eventId = document.getElementById('eventId');
    const eventName = document.getElementById('eventName');
    const eventType = document.getElementById('eventType');
    const eventPrice = document.getElementById('eventPrice');
    const eventStartDate = document.getElementById('eventStartDate');
    const eventEndDate = document.getElementById('eventEndDate');
    const eventDuration = document.getElementById('eventDuration');
    const eventDescription = document.getElementById('eventDescription');
    const eventImageUpload = document.getElementById('eventImageUpload');
    const eventImage = document.getElementById('eventImage');
    const imagePreview = document.getElementById('imagePreview');
    const eventActive = document.getElementById('eventActive');
    
    // Tabbed interface elements
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    const prevTabBtn = document.getElementById('prevTabBtn');
    const nextTabBtn = document.getElementById('nextTabBtn');
    const submitEventBtn = document.getElementById('submitEventBtn');
    let currentTabIndex = 0;
    
    // Vehicle selection elements
    const availableVehiclesContainer = document.getElementById('availableVehiclesContainer');
    const selectedVehiclesContainer = document.getElementById('selectedVehiclesContainer');
    const noVehiclesSelected = document.getElementById('noVehiclesSelected');
    const selectedVehicleCount = document.getElementById('selectedVehicleCount');
    
    // Selected vehicles array
    let selectedVehicles = [];
    
    // Check if admin is logged in
    checkAdminStatus();
    
    // Load events
    loadEvents();
    
    // Event listeners
    eventTypeFilter.addEventListener('change', loadEvents);
    statusFilter.addEventListener('change', loadEvents);
    addEventBtn.addEventListener('click', showAddEventForm);
    eventForm.addEventListener('submit', handleEventFormSubmit);
    cancelEventBtn.addEventListener('click', closeEventFormModal);
    
    // Tabbed interface event listeners
    tabButtons.forEach((button, index) => {
        button.addEventListener('click', () => switchTab(index));
    });
    
    prevTabBtn.addEventListener('click', () => switchTab(currentTabIndex - 1));
    nextTabBtn.addEventListener('click', () => {
        // Validate current tab before moving to the next
        if (validateCurrentTab()) {
            if (currentTabIndex === tabButtons.length - 1) {
                // Last tab - trigger form submission
                eventForm.dispatchEvent(new Event('submit'));
            } else {
                switchTab(currentTabIndex + 1);
            }
        }
    });
    
    // Image upload preview
    eventImageUpload.addEventListener('change', handleImageUpload);
    
    // Modal event listeners
    eventFormClose.addEventListener('click', closeEventFormModal);
    manageVehiclesClose.addEventListener('click', closeManageVehiclesModal);
    confirmDeleteClose.addEventListener('click', closeConfirmDeleteModal);
    cancelDeleteBtn.addEventListener('click', closeConfirmDeleteModal);
    
    vehicleSearchInput.addEventListener('input', filterAvailableVehicles);
    
    window.addEventListener('click', function(event) {
        if (event.target === eventFormModal) {
            closeEventFormModal();
        } else if (event.target === manageVehiclesModal) {
            closeManageVehiclesModal();
        } else if (event.target === confirmDeleteModal) {
            closeConfirmDeleteModal();
        }
    });
    
    // Tab switching function
    function switchTab(tabIndex) {
        if (tabIndex < 0 || tabIndex >= tabButtons.length) return;
        
        // Update active tab
        tabButtons.forEach((btn, i) => {
            btn.classList.toggle('active', i === tabIndex);
        });
        
        // Show only the selected tab content
        tabContents.forEach((content, i) => {
            content.style.display = i === tabIndex ? 'block' : 'none';
        });
        
        // Update current tab index
        currentTabIndex = tabIndex;
        
        // Update navigation buttons
        updateTabButtons();
    }
    
    function updateTabButtons() {
        // Show/hide prev button based on current tab
        prevTabBtn.style.display = currentTabIndex === 0 ? 'none' : 'inline-block';
        
        // Update next button text and show/hide submit button
        if (currentTabIndex === tabButtons.length - 1) {
            nextTabBtn.textContent = 'Finish';
            submitEventBtn.style.display = 'inline-block';
        } else {
            nextTabBtn.textContent = 'Next';
            submitEventBtn.style.display = 'none';
        }
    }
    
    function validateCurrentTab() {
        // Validate fields in the current tab
        switch(currentTabIndex) {
            case 0: // Basic Information
                if (!eventName.value || !eventType.value || !eventPrice.value || 
                    !eventStartDate.value || !eventEndDate.value || !eventDuration.value || 
                    !eventDescription.value) {
                    alert("Please fill in all required fields in the Basic Information tab");
                    return false;
                }
                
                const startDate = new Date(eventStartDate.value);
                const endDate = new Date(eventEndDate.value);
                
                if (endDate < startDate) {
                    alert("End date cannot be earlier than start date");
                    return false;
                }
                return true;
                
            case 1: // Vehicle Selection
                if (selectedVehicles.length === 0) {
                    alert("Please select at least one vehicle from the inventory");
                    return false;
                }
                return true;
                
            case 2: // Event Image
                if (eventImageUpload.files.length > 0) {
                    const file = eventImageUpload.files[0];
                    if (file.size > 5 * 1024 * 1024) {
                        alert("Image file size must be less than 5MB");
                        return false;
                    }
                    
                    const validTypes = ['image/jpeg', 'image/png', 'image/jpg'];
                    if (!validTypes.includes(file.type)) {
                        alert("Please upload a valid image file (JPG or PNG)");
                        return false;
                    }
                }
                return true;
        }
        return true;
    }
    
    // Image upload handler
    function handleImageUpload(e) {
        const file = e.target.files[0];
        if (!file) {
            // Clear image value if no file is selected
            imagePreview.innerHTML = '';
            eventImage.value = '';
            return;
        }
        
        // Validate file type
        const validTypes = ['image/jpeg', 'image/png', 'image/jpg'];
        if (!validTypes.includes(file.type)) {
            alert('Please upload a valid image file (JPG or PNG)');
            eventImageUpload.value = '';
            imagePreview.innerHTML = '';
            eventImage.value = '';
            return;
        }
        
        // Validate file size (5MB max)
        if (file.size > 5 * 1024 * 1024) {
            alert('Image size should be less than 5MB');
            eventImageUpload.value = '';
            imagePreview.innerHTML = '';
            eventImage.value = '';
            return;
        }
        
        // Show preview and compress/resize the image
        const reader = new FileReader();
        reader.onload = function(e) {
            try {
                const img = new Image();
                img.onload = function() {
                    // Resize image if it's too large
                    const maxWidth = 800;
                    const maxHeight = 600;
                    
                    let width = img.width;
                    let height = img.height;
                    
                    // Calculate new dimensions
                    if (width > maxWidth || height > maxHeight) {
                        if (width > height) {
                            if (width > maxWidth) {
                                height = Math.round(height * (maxWidth / width));
                                width = maxWidth;
                            }
                        } else {
                            if (height > maxHeight) {
                                width = Math.round(width * (maxHeight / height));
                                height = maxHeight;
                            }
                        }
                    }
                    
                    // Create canvas for resizing
                    const canvas = document.createElement('canvas');
                    canvas.width = width;
                    canvas.height = height;
                    
                    // Draw resized image
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(img, 0, 0, width, height);
                    
                    // Get resized image data
                    const resizedImageData = canvas.toDataURL('image/jpeg', 0.8); // Use 80% quality
                    
                    // Update preview and set value
                    imagePreview.innerHTML = `<img src="${resizedImageData}" alt="Preview">`;
                    eventImage.value = resizedImageData;
                    
                    // Check final size
                    if (resizedImageData.length > 500000) { // 500KB limit
                        console.warn('Resized image is still large (' + Math.round(resizedImageData.length/1024) + 'KB). Consider using the file upload endpoint instead.');
                    }
                };
                img.src = e.target.result;
            } catch (error) {
                console.error('Error processing image:', error);
                alert('Error processing image. Please try a different one.');
                eventImageUpload.value = '';
                imagePreview.innerHTML = '';
                eventImage.value = '';
            }
        };
        reader.readAsDataURL(file);
    }
    
    // Functions
    function loadEvents() {
        eventsTableBody.innerHTML = `
            <tr>
                <td colspan="9" class="loading-cell">
                    <i class="fas fa-spinner fa-spin"></i> Loading events...
                </td>
            </tr>
        `;
        
        fetch('/api/events')
            .then(response => response.json())
            .then(events => {
                // Apply filters
                const selectedType = eventTypeFilter.value;
                const selectedStatus = statusFilter.value;
                
                let filteredEvents = events;
                
                if (selectedType !== 'all') {
                    filteredEvents = filteredEvents.filter(event => event.eventType === selectedType);
                }
                
                if (selectedStatus !== 'all') {
                    const isActive = selectedStatus === 'active';
                    filteredEvents = filteredEvents.filter(event => event.active === isActive);
                }
                
                if (filteredEvents.length === 0) {
                    eventsTableBody.innerHTML = '';
                    emptyEvents.style.display = 'flex';
                } else {
                    eventsTableBody.innerHTML = '';
                    emptyEvents.style.display = 'none';
                    
                    filteredEvents.forEach(event => {
                        const row = createEventRow(event);
                        eventsTableBody.appendChild(row);
                    });
                }
            })
            .catch(error => {
                console.error('Error loading events:', error);
                eventsTableBody.innerHTML = `
                    <tr>
                        <td colspan="9" class="error-cell">
                            <i class="fas fa-exclamation-circle"></i> 
                            Failed to load events. Please try again later.
                        </td>
                    </tr>
                `;
            });
    }
    
    function createEventRow(event) {
        const row = document.createElement('tr');
        
        // Format dates
        const startDate = event.startDate ? new Date(event.startDate).toLocaleDateString() : 'N/A';
        const endDate = event.endDate ? new Date(event.endDate).toLocaleDateString() : 'N/A';
        
        row.innerHTML = `
            <td>${event.id}</td>
            <td>${event.name}</td>
            <td>${formatEventType(event.eventType)}</td>
            <td>Rs ${event.price.toFixed(2)}</td>
            <td>${event.durationHours} hours</td>
            <td>${startDate} - ${endDate}</td>
            <td>
                <span class="status-badge ${event.active ? 'active' : 'inactive'}">
                    ${event.active ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td>${event.vehicles ? event.vehicles.length : 0}</td>
            <td class="actions-cell">
                <button class="btn-icon edit-btn" title="Edit Event">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn-icon vehicles-btn" title="Manage Vehicles">
                    <i class="fas fa-car"></i>
                </button>
                <button class="btn-icon ${event.active ? 'deactivate-btn' : 'activate-btn'}" 
                        title="${event.active ? 'Deactivate' : 'Activate'} Event">
                    <i class="fas ${event.active ? 'fa-toggle-on' : 'fa-toggle-off'}"></i>
                </button>
                <button class="btn-icon delete-btn" title="Delete Event">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        
        // Add event listeners
        row.querySelector('.edit-btn').addEventListener('click', () => showEditEventForm(event));
        row.querySelector('.vehicles-btn').addEventListener('click', () => showManageVehiclesModal(event));
        
        const activateDeactivateBtn = row.querySelector(event.active ? '.deactivate-btn' : '.activate-btn');
        activateDeactivateBtn.addEventListener('click', () => toggleEventStatus(event));
        
        row.querySelector('.delete-btn').addEventListener('click', () => showDeleteConfirmation(event));
        
        return row;
    }
    
    function showAddEventForm() {
        eventFormTitle.textContent = 'Create New Event';
        eventForm.reset();
        eventId.value = '';
        
        // Set default values for required fields
        eventName.value = '';
        eventType.value = ''; // Will show the "Select Event Type" option
        eventPrice.value = '0'; // Default price
        
        // Set default date values to today and tomorrow
        const today = new Date();
        const tomorrow = new Date();
        tomorrow.setDate(today.getDate() + 1);
        
        const formatDate = (date) => {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        };
        
        eventStartDate.value = formatDate(today);
        eventEndDate.value = formatDate(tomorrow);
        
        eventDuration.value = '1'; // Default duration
        eventDescription.value = ''; // Empty description
        
        eventActive.checked = true;
        imagePreview.innerHTML = '';
        eventImage.value = ''; // Clear any previous image value
        
        // Reset selected vehicles
        selectedVehicles = [];
        updateSelectedVehiclesDisplay();
        
        // Load available vehicles from inventory
        loadAvailableVehiclesForForm();
        
        // Set button text to "Create Event"
        submitEventBtn.textContent = 'Create Event';
        
        // Show the first tab
        switchTab(0);
        
        eventFormModal.style.display = 'block';
    }
    
    function showEditEventForm(event) {
        eventFormTitle.textContent = 'Edit Event';
        
        eventId.value = event.id;
        eventName.value = event.name;
        eventType.value = event.eventType;
        eventPrice.value = event.price;
        
        // Set dates if available
        if (event.startDate) {
            eventStartDate.value = new Date(event.startDate).toISOString().split('T')[0];
        }
        if (event.endDate) {
            eventEndDate.value = new Date(event.endDate).toISOString().split('T')[0];
        }
        
        eventDuration.value = event.durationHours;
        eventDescription.value = event.description;
        
        // Display image if available
        if (event.imagePath) {
            imagePreview.innerHTML = `<img src="${event.imagePath}" alt="Event Image">`;
            eventImage.value = event.imagePath;
        } else {
            imagePreview.innerHTML = '';
        }
        
        eventActive.checked = event.active;
        
        // Load the event's vehicles
        selectedVehicles = event.vehicles ? [...event.vehicles] : [];
        updateSelectedVehiclesDisplay();
        
        // Load available vehicles
        loadAvailableVehiclesForForm(event.id);
        
        // Set button text to "Update Event"
        submitEventBtn.textContent = 'Update Event';
        
        // Show the first tab
        switchTab(0);
        
        eventFormModal.style.display = 'block';
    }
    
    function loadAvailableVehiclesForForm(eventId = null) {
        availableVehiclesContainer.innerHTML = `
            <p class="loading"><i class="fas fa-spinner fa-spin"></i> Loading inventory vehicles...</p>
        `;
        
        const url = eventId ? `/api/events/${eventId}/available-vehicles` : '/api/vehicles';
        
        fetch(url)
            .then(response => response.json())
            .then(vehicles => {
                if (vehicles.length === 0) {
                    availableVehiclesContainer.innerHTML = `
                        <p class="empty-message">No available vehicles found in inventory.</p>
                    `;
                } else {
                    availableVehiclesContainer.innerHTML = '';
                    
                    // Add a count of available vehicles
                    const vehicleCountDiv = document.createElement('div');
                    vehicleCountDiv.className = 'vehicle-count';
                    vehicleCountDiv.innerHTML = `<p><strong>${vehicles.length}</strong> vehicles available</p>`;
                    availableVehiclesContainer.appendChild(vehicleCountDiv);
                    
                    vehicles.forEach(vehicle => {
                        const isSelected = selectedVehicles.some(v => v.vehicleID === vehicle.vehicleID);
                        const vehicleCard = createFormVehicleCard(vehicle, isSelected);
                        availableVehiclesContainer.appendChild(vehicleCard);
                    });
                }
            })
            .catch(error => {
                console.error('Error loading vehicles:', error);
                availableVehiclesContainer.innerHTML = `
                    <p class="error-message">
                        <i class="fas fa-exclamation-circle"></i> 
                        Failed to load inventory vehicles. Please try again.
                    </p>
                `;
            });
    }
    
    function createFormVehicleCard(vehicle, isSelected) {
        const card = document.createElement('div');
        card.className = `vehicle-card ${isSelected ? 'selected' : ''}`;
        card.dataset.vehicleId = vehicle.vehicleID;
        
        let vehicleTypeDisplay = getVehicleType(vehicle);
        let vehicleTypeClass = getVehicleTypeClass(vehicle);
        
        card.innerHTML = `
            <div class="vehicle-card-header">
                <span class="vehicle-type ${vehicleTypeClass}">${vehicleTypeDisplay}</span>
                <span class="vehicle-id">${vehicle.vehicleID}</span>
            </div>
            <div class="vehicle-card-body">
                <h4>${vehicle.brand} ${vehicle.model}</h4>
                <p class="vehicle-details">
                    <span class="vehicle-price">Rs ${vehicle.rentPrice.toFixed(2)}/day</span>
                </p>
            </div>
            <div class="vehicle-card-footer">
                <button type="button" class="btn-sm ${isSelected ? 'btn-danger' : 'btn-primary'}">
                    <i class="fas ${isSelected ? 'fa-trash-alt' : 'fa-plus'}"></i>
                    ${isSelected ? 'Remove' : 'Add Vehicle'}
                </button>
            </div>
        `;
        
        const button = card.querySelector('button');
        if (isSelected) {
            button.addEventListener('click', () => removeVehicleFromSelection(vehicle));
        } else {
            button.addEventListener('click', () => addVehicleToSelection(vehicle));
        }
        
        return card;
    }
    
    function addVehicleToSelection(vehicle) {
        // Check if vehicle is already in selection
        const exists = selectedVehicles.some(v => v.vehicleID === vehicle.vehicleID);
        if (!exists) {
            selectedVehicles.push(vehicle);
            updateSelectedVehiclesDisplay();
            
            // Update UI
            const card = document.querySelector(`.vehicle-card[data-vehicle-id="${vehicle.vehicleID}"]`);
            if (card) {
                card.classList.add('selected');
                const button = card.querySelector('button');
                button.className = 'btn-sm btn-danger';
                button.innerHTML = '<i class="fas fa-trash-alt"></i> Remove';
                button.removeEventListener('click', () => addVehicleToSelection(vehicle));
                button.addEventListener('click', () => removeVehicleFromSelection(vehicle));
            }
        }
    }
    
    function removeVehicleFromSelection(vehicle) {
        // Remove vehicle from selected list
        selectedVehicles = selectedVehicles.filter(v => v.vehicleID !== vehicle.vehicleID);
        updateSelectedVehiclesDisplay();
        
        // Update button in available vehicles list
        const availableCard = availableVehiclesContainer.querySelector(`.vehicle-card[data-vehicle-id="${vehicle.vehicleID}"]`);
        if (availableCard) {
            availableCard.classList.remove('selected');
            const button = availableCard.querySelector('button');
            button.className = 'btn-sm btn-primary';
            button.innerHTML = '<i class="fas fa-plus"></i> Add Vehicle';
            
            // Update event listener
            button.removeEventListener('click', () => removeVehicleFromSelection(vehicle));
            button.addEventListener('click', () => addVehicleToSelection(vehicle));
        }
    }
    
    function updateSelectedVehiclesDisplay() {
        // Update selected vehicle count
        selectedVehicleCount.textContent = selectedVehicles.length;
        
        // Update selected vehicles container
        selectedVehiclesContainer.innerHTML = '';
        
        // Show "no vehicles selected" message if no vehicles are selected
        if (selectedVehicles.length === 0) {
            noVehiclesSelected.style.display = 'block';
        } else {
            noVehiclesSelected.style.display = 'none';
            
            // Create and append vehicle items
            selectedVehicles.forEach(vehicle => {
                const vehicleItem = document.createElement('div');
                vehicleItem.className = 'selected-vehicle-item';
                vehicleItem.dataset.vehicleId = vehicle.vehicleID;
                
                vehicleItem.innerHTML = `
                    <span class="vehicle-info">
                        <span class="vehicle-type-badge ${getVehicleTypeClass(vehicle)}">${getVehicleType(vehicle)}</span>
                        ${vehicle.brand} ${vehicle.model}
                    </span>
                    <button class="btn-icon remove-btn" title="Remove Vehicle">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                
                // Add event listener to remove button
                const removeBtn = vehicleItem.querySelector('.remove-btn');
                removeBtn.addEventListener('click', () => removeVehicleFromSelection(vehicle));
                
                selectedVehiclesContainer.appendChild(vehicleItem);
            });
        }
    }
    
    function handleEventFormSubmit(e) {
        e.preventDefault();
        
        // Validate all tabs
        for (let i = 0; i < tabButtons.length; i++) {
            currentTabIndex = i;
            if (!validateCurrentTab()) {
                switchTab(i);
                return;
            }
        }
        
        // Extract just the vehicle IDs from the selected vehicles array
        const vehicleIds = selectedVehicles.map(vehicle => vehicle.vehicleID);
        
        // Check if any vehicles are selected
        if (vehicleIds.length === 0) {
            alert("Please select at least one vehicle for the event");
            switchTab(1); // Switch to vehicle selection tab
            return;
        }
        
        const formData = {
            name: eventName.value.trim(),
            eventType: eventType.value,
            price: parseFloat(eventPrice.value) || 0,
            startDateStr: eventStartDate.value, 
            endDateStr: eventEndDate.value,
            durationHours: parseInt(eventDuration.value) || 1,
            description: eventDescription.value.trim(),
            active: eventActive.checked,
            vehicleIds: vehicleIds
        };
        
        // Ensure description is not too long - some databases have limits
        if (formData.description && formData.description.length > 2000) {
            formData.description = formData.description.substring(0, 2000);
        }
        
        // Only add imagePath if it has a value to avoid sending empty strings
        if (eventImage.value && eventImage.value.trim() !== '') {
            // If the image is very large, don't send it directly
            if (eventImage.value.length > 1000000) { // 1MB limit
                alert("Image data is too large. Please choose a smaller image or upload it separately.");
                switchTab(2); // Switch to image tab
                return;
            }
            formData.imagePath = eventImage.value;
        }
        
        const id = eventId.value;
        const isEdit = id !== '';
        
        const url = isEdit ? `/api/events/${id}` : '/api/events';
        const method = isEdit ? 'PUT' : 'POST';
        
        if (isEdit) {
            formData.id = parseInt(id);
        }
        
        // Show loading state
        const submitBtn = submitEventBtn;
        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
        
        console.log('Sending event data:', JSON.stringify(formData));
        
        fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    try {
                        // Try to parse as JSON first
                        const errorData = JSON.parse(text);
                        throw new Error(errorData.message || text || 'Failed to save event');
                    } catch (e) {
                        // If not JSON, use the text as is
                        throw new Error(text || 'Failed to save event');
                    }
                });
            }
            return response.json();
        })
        .then(event => {
            alert(`Event ${isEdit ? 'updated' : 'created'} successfully.`);
            closeEventFormModal();
            loadEvents();
        })
        .catch(error => {
            console.error('Error saving event:', error);
            let errorMessage = error.message;
            
            // Check if the error message contains database-related keywords
            const dbErrorKeywords = [
                'database', 'db', 'sql', 'constraint', 'violation',
                'integrity', 'foreign key', 'primary key', 'unique'
            ];
            
            const isDbError = dbErrorKeywords.some(keyword => 
                errorMessage.toLowerCase().includes(keyword.toLowerCase()));
                
            if (isDbError) {
                errorMessage = 'Database error occurred. This might be due to:' +
                    '\n- Missing required information' +
                    '\n- Database connection issues' +
                    '\n- Conflict with existing data';
            }
            // Check if the error message is too long or technical
            else if (errorMessage.length > 200) {
                errorMessage = 'Failed to save event. Please check all fields and try again.';
            }
            
            alert('Failed to save event: ' + errorMessage);
            
            // Enable the submit button to allow retry
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        })
        .finally(() => {
            // Reset button state
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        });
    }
    
    function showManageVehiclesModal(event) {
        manageVehiclesEventName.textContent = event.name;
        
        // Load current event vehicles
        loadCurrentVehicles(event.id);
        
        // Load available vehicles
        loadAvailableVehicles(event.id);
        
        manageVehiclesModal.style.display = 'block';
        manageVehiclesModal.dataset.eventId = event.id;
    }
    
    function loadCurrentVehicles(eventId) {
        currentVehiclesList.innerHTML = `
            <p class="loading"><i class="fas fa-spinner fa-spin"></i> Loading vehicles...</p>
        `;
        
        fetch(`/api/events/${eventId}/vehicles`)
            .then(response => response.json())
            .then(vehicles => {
                // Update vehicle count
                currentVehicleCount.textContent = vehicles.length;
                
                if (vehicles.length === 0) {
                    currentVehiclesList.innerHTML = '';
                    const emptyMessage = currentVehiclesList.querySelector('.empty-list-message');
                    if (emptyMessage) {
                        emptyMessage.style.display = 'block';
                    } else {
                        currentVehiclesList.innerHTML = `
                            <p class="empty-list-message">No vehicles assigned to this event yet.</p>
                        `;
                    }
                } else {
                    currentVehiclesList.innerHTML = '';
                    const emptyMessage = currentVehiclesList.querySelector('.empty-list-message');
                    if (emptyMessage) {
                        emptyMessage.style.display = 'none';
                    }
                    
                    vehicles.forEach(vehicle => {
                        const card = createVehicleCard(vehicle, true);
                        currentVehiclesList.appendChild(card);
                    });
                }
            })
            .catch(error => {
                console.error('Error loading event vehicles:', error);
                currentVehiclesList.innerHTML = `
                    <p class="error-message">
                        <i class="fas fa-exclamation-circle"></i> 
                        Failed to load vehicles. Please try again.
                    </p>
                `;
            });
    }
    
    function loadAvailableVehicles(eventId) {
        availableVehiclesList.innerHTML = `
            <p class="loading"><i class="fas fa-spinner fa-spin"></i> Loading vehicles...</p>
        `;
        
        fetch(`/api/events/${eventId}/available-vehicles`)
            .then(response => response.json())
            .then(vehicles => {
                if (vehicles.length === 0) {
                    availableVehiclesList.innerHTML = `
                        <p class="empty-message">No available vehicles found.</p>
                    `;
                } else {
                    availableVehiclesList.innerHTML = '';
                    
                    vehicles.forEach(vehicle => {
                        const card = createVehicleCard(vehicle, false);
                        availableVehiclesList.appendChild(card);
                    });
                }
            })
            .catch(error => {
                console.error('Error loading available vehicles:', error);
                availableVehiclesList.innerHTML = `
                    <p class="error-message">
                        <i class="fas fa-exclamation-circle"></i> 
                        Failed to load vehicles. Please try again.
                    </p>
                `;
            });
    }
    
    function createVehicleCard(vehicle, isCurrentVehicle) {
        const card = document.createElement('div');
        card.className = 'vehicle-card';
        card.dataset.vehicleId = vehicle.id;
        
        card.innerHTML = `
            <div class="vehicle-card-header">
                <span class="vehicle-type ${getVehicleTypeClass(vehicle)}">${getVehicleType(vehicle)}</span>
                <span class="vehicle-id">${vehicle.id}</span>
            </div>
            <div class="vehicle-card-body">
                <h4>${vehicle.make} ${vehicle.model}</h4>
                <p class="vehicle-details">
                    <span class="vehicle-year">${vehicle.year}</span>
                    <span class="vehicle-color" style="background-color: ${vehicle.color}"></span>
                </p>
            </div>
            <div class="vehicle-card-footer">
                <button class="btn-sm ${isCurrentVehicle ? 'btn-danger' : 'btn-primary'}">
                    <i class="fas ${isCurrentVehicle ? 'fa-minus' : 'fa-plus'}"></i>
                    ${isCurrentVehicle ? 'Remove' : 'Add'}
                </button>
            </div>
        `;
        
        const eventId = manageVehiclesModal.dataset.eventId;
        const button = card.querySelector('button');
        
        if (isCurrentVehicle) {
            button.addEventListener('click', () => removeVehicleFromEvent(vehicle.id));
        } else {
            button.addEventListener('click', () => addVehicleToEvent(vehicle.id));
        }
        
        return card;
    }
    
    function addVehicleToEvent(vehicleId) {
        const eventId = manageVehiclesModal.dataset.eventId;
        
        fetch(`/api/events/${eventId}/vehicles/${vehicleId}`, {
            method: 'POST'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to add vehicle to event');
            }
            return response.json();
        })
        .then(() => {
            // Reload vehicles lists
            loadCurrentVehicles(eventId);
            loadAvailableVehicles(eventId);
            // Reload events to update the count
            loadEvents();
        })
        .catch(error => {
            console.error('Error adding vehicle:', error);
            alert('Failed to add vehicle to event. Please try again.');
        });
    }
    
    function removeVehicleFromEvent(vehicleId) {
        const eventId = manageVehiclesModal.dataset.eventId;
        
        fetch(`/api/events/${eventId}/vehicles/${vehicleId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to remove vehicle from event');
            }
            return response.json();
        })
        .then(() => {
            // Reload vehicles lists
            loadCurrentVehicles(eventId);
            loadAvailableVehicles(eventId);
            // Reload events to update the count
            loadEvents();
        })
        .catch(error => {
            console.error('Error removing vehicle:', error);
            alert('Failed to remove vehicle from event. Please try again.');
        });
    }
    
    function filterAvailableVehicles() {
        const searchTerm = vehicleSearchInput.value.toLowerCase();
        const vehicleCards = availableVehiclesList.querySelectorAll('.vehicle-card');
        
        vehicleCards.forEach(card => {
            const vehicleId = card.querySelector('.vehicle-id').textContent.toLowerCase();
            const vehicleType = card.querySelector('.vehicle-type').textContent.toLowerCase();
            const vehicleName = card.querySelector('h4').textContent.toLowerCase();
            
            if (vehicleId.includes(searchTerm) || vehicleType.includes(searchTerm) || vehicleName.includes(searchTerm)) {
                card.style.display = 'flex';
            } else {
                card.style.display = 'none';
            }
        });
    }
    
    function getVehicleTypeClass(vehicle) {
        // For simplicity, just return a default class if we can't determine type
        return 'vehicle-type-standard';
    }
    
    function getVehicleType(vehicle) {
        // Default to a standard type if we can't determine from the data
        return 'Standard';
    }
    
    function toggleEventStatus(event) {
        const action = event.active ? 'deactivate' : 'activate';
        
        fetch(`/api/events/${event.id}/${action}`, {
            method: 'PUT'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to ${action} event`);
            }
            return response.json();
        })
        .then(() => {
            alert(`Event ${action}d successfully.`);
            loadEvents();
        })
        .catch(error => {
            console.error(`Error ${action}ing event:`, error);
            alert(`Failed to ${action} event. Please try again.`);
        });
    }
    
    function showDeleteConfirmation(event) {
        deleteEventName.textContent = event.name;
        confirmDeleteModal.style.display = 'block';
        
        // Set up delete handler
        const handleDelete = () => {
            deleteEvent(event.id);
            confirmDeleteBtn.removeEventListener('click', handleDelete);
        };
        
        confirmDeleteBtn.addEventListener('click', handleDelete);
    }
    
    function deleteEvent(eventId) {
        fetch(`/api/events/${eventId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to delete event');
            }
            alert('Event deleted successfully.');
            closeConfirmDeleteModal();
            loadEvents();
        })
        .catch(error => {
            console.error('Error deleting event:', error);
            alert('Failed to delete event. Please try again.');
        });
    }
    
    function closeEventFormModal() {
        eventFormModal.style.display = 'none';
    }
    
    function closeManageVehiclesModal() {
        manageVehiclesModal.style.display = 'none';
        manageVehiclesModal.dataset.eventId = '';
    }
    
    function closeConfirmDeleteModal() {
        confirmDeleteModal.style.display = 'none';
    }
    
    function formatEventType(type) {
        if (!type) return 'Unknown';
        
        return type
            .replace('_', ' ')
            .toLowerCase()
            .replace(/\b\w/g, char => char.toUpperCase());
    }
    
    function checkAdminStatus() {
        const adminToken = localStorage.getItem('adminToken');
        if (!adminToken) {
            window.location.href = 'login.html';
        }
        
        // Set admin name if available
        const adminName = localStorage.getItem('adminName');
        if (adminName) {
            document.getElementById('userName').textContent = adminName;
        }
        
        // Set up logout button
        document.getElementById('logoutButton').addEventListener('click', function(e) {
            e.preventDefault();
            localStorage.removeItem('adminToken');
            localStorage.removeItem('adminName');
            window.location.href = 'login.html';
        });
    }
});
