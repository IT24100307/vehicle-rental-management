// Inventory page functionality

// Global variables
let vehicles = [];
let selectedVehicle = null;
let currentSortOrder = 'none';

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in
    const customer = checkAuth();
    if (!customer) return;
    
    // Initialize the page
    initSortButtons();
    initAddVehicleForm();
    initVehicleDetailsModal();
    initRentVehicleModal();
    
    // Load vehicles
    loadVehicles();
    
    // Load rented vehicles
    loadRentedVehicles();
});

// Initialize sort buttons
function initSortButtons() {
    const sortButtons = document.querySelectorAll('.sort-btn');
    
    sortButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Remove active class from all buttons
            sortButtons.forEach(btn => btn.classList.remove('active'));
            
            // Add active class to clicked button
            this.classList.add('active');
            
            // Get sort order
            currentSortOrder = this.dataset.sort;
            
            // Apply sorting
            if (currentSortOrder === 'none') {
                loadVehicles();
            } else {
                sortVehiclesByPrice(currentSortOrder);
            }
        });
    });
}

// Sort vehicles by price using selection sort
function sortVehiclesByPrice(order) {
    try {
        const vehicleGrid = document.getElementById('vehicleGrid');
        vehicleGrid.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Sorting vehicles...</div>';
        
        // Clone the array to avoid modifying the original
        let sortedVehicles = [...vehicles];
        
        // Selection Sort Algorithm
        for (let i = 0; i < sortedVehicles.length - 1; i++) {
            let minMaxIndex = i;
            
            for (let j = i + 1; j < sortedVehicles.length; j++) {
                // For ascending order
                if (order === 'asc' && sortedVehicles[j].rentPrice < sortedVehicles[minMaxIndex].rentPrice) {
                    minMaxIndex = j;
                } 
                // For descending order
                else if (order === 'desc' && sortedVehicles[j].rentPrice > sortedVehicles[minMaxIndex].rentPrice) {
                    minMaxIndex = j;
                }
            }
            
            // Swap if needed
            if (minMaxIndex !== i) {
                const temp = sortedVehicles[i];
                sortedVehicles[i] = sortedVehicles[minMaxIndex];
                sortedVehicles[minMaxIndex] = temp;
            }
        }
        
        // Render sorted vehicles
        renderVehicles(sortedVehicles);
    } catch (error) {
        console.error('Error sorting vehicles:', error);
        const vehicleGrid = document.getElementById('vehicleGrid');
        vehicleGrid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-circle fa-3x"></i>
                <h2>Error Sorting Vehicles</h2>
                <p>${error.message || 'Failed to sort vehicles. Please try again.'}</p>
            </div>
        `;
    }
}

// Load all vehicles
async function loadVehicles() {
    try {
        const vehicleGrid = document.getElementById('vehicleGrid');
        vehicleGrid.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading vehicles...</div>';
        
        // Call the API to get all vehicles
        const response = await apiRequest('/vehicles/view');
        vehicles = response;
        
        // Render vehicles
        renderVehicles(vehicles);
    } catch (error) {
        console.error('Error loading vehicles:', error);
        const vehicleGrid = document.getElementById('vehicleGrid');
        vehicleGrid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-circle fa-3x"></i>
                <h2>Error Loading Vehicles</h2>
                <p>${error.message || 'Failed to load vehicles. Please try again.'}</p>
            </div>
        `;
    }
}

// Filter vehicles by type - deprecated, kept for reference
function filterVehiclesByType(type) {
    console.warn('filterVehiclesByType is deprecated. Use sortVehiclesByPrice instead.');
    // This function has been replaced by sortVehiclesByPrice
}

// Render vehicles to grid
function renderVehicles(vehicleList) {
    const vehicleGrid = document.getElementById('vehicleGrid');
    
    // If no vehicles, show empty state
    if (!vehicleList || vehicleList.length === 0) {
        vehicleGrid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-car-side fa-3x"></i>
                <h2>No Vehicles Found</h2>
                <p>There are no vehicles available that match your criteria.</p>
            </div>
        `;
        return;
    }
    
    // Clear grid
    vehicleGrid.innerHTML = '';
    
    // Add each vehicle to grid
    vehicleList.forEach(vehicle => {
        // Create vehicle card
        const vehicleCard = document.createElement('div');
        vehicleCard.className = 'vehicle-card';
          // Determine vehicle type text
        let vehicleType = 'Vehicle';
        // If vehicle has explicit vehicleType property, use it
        if (vehicle.hasOwnProperty('vehicleType')) {
            switch(vehicle.vehicleType) {
                case 1:
                    vehicleType = 'Car';
                    break;
                case 2:
                    vehicleType = 'Van';
                    break;
                case 3:
                    vehicleType = 'Bike';
                    break;
                case 4:
                    vehicleType = 'Bus';
                    break;
                case 5:
                    vehicleType = 'Lorry';
                    break;
                default:
                    vehicleType = 'Vehicle';
            }
        } else {
            // Fallback to existing property checking for backward compatibility
            if (vehicle.hasOwnProperty('numberOfDoors')) vehicleType = 'Car';
            else if (vehicle.hasOwnProperty('cargoCapacity')) vehicleType = 'Van';
            else if (vehicle.hasOwnProperty('engineCapacity')) vehicleType = 'Bike';
            else if (vehicle.hasOwnProperty('seatingCapacity')) vehicleType = 'Bus';
            else if (vehicle.hasOwnProperty('maxLoad')) vehicleType = 'Lorry';
        }
        
        // Set card content
        vehicleCard.innerHTML = `
            <img src="${vehicle.imagePath || 'https://via.placeholder.com/300x200?text=No+Image'}" alt="${vehicle.brand} ${vehicle.model}" class="vehicle-img">
            <div class="vehicle-info">
                <h3 class="vehicle-title">${vehicle.brand} ${vehicle.model}</h3>
                <div class="vehicle-price">${formatCurrency(vehicle.rentPrice)} per day</div>
                <div class="vehicle-type">Type: ${vehicleType}</div>
                <div class="vehicle-id">ID: ${vehicle.vehicleID}</div>
                <div class="vehicle-actions">
                    <button class="btn btn-primary view-details-btn" data-id="${vehicle.vehicleID}">View Details</button>
                    <button class="btn btn-secondary rent-btn" data-id="${vehicle.vehicleID}">Rent Now</button>
                </div>
            </div>
        `;
        
        // Add event listeners
        const viewDetailsBtn = vehicleCard.querySelector('.view-details-btn');
        const rentBtn = vehicleCard.querySelector('.rent-btn');
        
        viewDetailsBtn.addEventListener('click', () => openVehicleDetailsModal(vehicle));
        rentBtn.addEventListener('click', () => openRentVehicleModal(vehicle));
        
        // Add card to grid
        vehicleGrid.appendChild(vehicleCard);
    });
}

// Load rented vehicles for linked list display
async function loadRentedVehicles() {
    const customer = JSON.parse(localStorage.getItem('customer'));
    if (!customer) return;
    
    try {
        const rentedVehiclesList = document.getElementById('rentedVehiclesList');
        rentedVehiclesList.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading rented vehicles...</div>';
        
        // Call the API to get customer rentals
        const response = await apiRequest(`/customers/${customer.customerId}/rentals`);
        
        // If no rentals, show empty state
        if (!response || response.length === 0) {
            rentedVehiclesList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-car-side fa-3x"></i>
                    <h2>No Rented Vehicles</h2>
                    <p>You haven't rented any vehicles yet.</p>
                </div>
            `;
            return;
        }
        
        // Clear list
        rentedVehiclesList.innerHTML = '';
        
        // Add each rental to linked list
        response.forEach(rental => {
            const node = document.createElement('div');
            node.className = 'linked-list-node';
            
            const vehicle = rental.vehicle;
            
            node.innerHTML = `
                <div class="node-title">${vehicle.brand} ${vehicle.model}</div>
                <div class="node-info">
                    <div>ID: ${rental.purchaseId}</div>
                    <div>Daily Rate: ${formatCurrency(vehicle.rentPrice)}</div>
                    <div>Days: ${rental.rentalDays}</div>
                    <div>Total: ${formatCurrency(vehicle.rentPrice * rental.rentalDays)}</div>
                    <div>Date: ${formatDate(rental.purchaseDate)}</div>
                </div>
            `;
            
            rentedVehiclesList.appendChild(node);
        });
    } catch (error) {
        console.error('Error loading rented vehicles:', error);
        const rentedVehiclesList = document.getElementById('rentedVehiclesList');
        rentedVehiclesList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-circle fa-3x"></i>
                <h2>Error Loading Rentals</h2>
                <p>${error.message || 'Failed to load rented vehicles. Please try again.'}</p>
            </div>
        `;
    }
}

// Open vehicle details modal
function openVehicleDetailsModal(vehicle) {
    selectedVehicle = vehicle;
    
    const modal = document.getElementById('vehicleDetailsModal');
    const title = document.getElementById('vehicleDetailsTitle');
    const body = document.getElementById('vehicleDetailsBody');
    
    title.textContent = `${vehicle.brand} ${vehicle.model} Details`;
      // Determine vehicle type and specific properties
    let vehicleType = 'Standard Vehicle';
    let specificProperties = '';
    
    // If vehicle has explicit vehicleType property, use it
    if (vehicle.hasOwnProperty('vehicleType')) {
        switch(vehicle.vehicleType) {
            case 1:
                vehicleType = 'Car';
                specificProperties = `
                    <div><strong>Number of Doors:</strong> ${vehicle.numberOfDoors}</div>
                    <div><strong>Transmission:</strong> ${vehicle.transmissionType}</div>
                `;
                break;
            case 2:
                vehicleType = 'Van';
                specificProperties = `
                    <div><strong>Cargo Capacity:</strong> ${vehicle.cargoCapacity} kg</div>
                `;
                break;
            case 3:
                vehicleType = 'Bike';
                specificProperties = `
                    <div><strong>Engine Capacity:</strong> ${vehicle.engineCapacity} cc</div>
                `;
                break;
            case 4:
                vehicleType = 'Bus';
                specificProperties = `
                    <div><strong>Seating Capacity:</strong> ${vehicle.seatingCapacity} passengers</div>
                `;
                break;
            case 5:
                vehicleType = 'Lorry';
                specificProperties = `
                    <div><strong>Maximum Load:</strong> ${vehicle.maxLoad} tons</div>
                `;
                break;
            default:
                vehicleType = 'Standard Vehicle';
        }
    } else {
        // Fallback to existing property checking for backward compatibility
        if (vehicle.hasOwnProperty('numberOfDoors')) {
            vehicleType = 'Car';
            specificProperties = `
                <div><strong>Number of Doors:</strong> ${vehicle.numberOfDoors}</div>
                <div><strong>Transmission:</strong> ${vehicle.transmissionType}</div>
            `;
        } else if (vehicle.hasOwnProperty('cargoCapacity')) {
            vehicleType = 'Van';
            specificProperties = `
                <div><strong>Cargo Capacity:</strong> ${vehicle.cargoCapacity} kg</div>
            `;
        } else if (vehicle.hasOwnProperty('engineCapacity')) {
            vehicleType = 'Bike';
            specificProperties = `
                <div><strong>Engine Capacity:</strong> ${vehicle.engineCapacity} cc</div>
            `;
        } else if (vehicle.hasOwnProperty('seatingCapacity')) {
            vehicleType = 'Bus';
            specificProperties = `
                <div><strong>Seating Capacity:</strong> ${vehicle.seatingCapacity} passengers</div>
            `;
        } else if (vehicle.hasOwnProperty('maxLoad')) {
            vehicleType = 'Lorry';
            specificProperties = `
                <div><strong>Maximum Load:</strong> ${vehicle.maxLoad} tons</div>
            `;
        }
    }
    
    // Build body content
    body.innerHTML = `
        <div class="vehicle-details-content">
            <img src="${vehicle.imagePath || 'https://via.placeholder.com/400x250?text=No+Image'}" alt="${vehicle.brand} ${vehicle.model}" class="vehicle-detail-img">
            
            <div class="vehicle-info-section">
                <h3>General Information</h3>
                <div><strong>ID:</strong> ${vehicle.vehicleID}</div>
                <div><strong>Type:</strong> ${vehicleType}</div>
                <div><strong>Brand:</strong> ${vehicle.brand}</div>
                <div><strong>Model:</strong> ${vehicle.model}</div>
                <div><strong>Rent Price:</strong> ${formatCurrency(vehicle.rentPrice)} per day</div>
            </div>
            
            <div class="vehicle-info-section">
                <h3>Specific Details</h3>
                ${specificProperties}
            </div>
        </div>
    `;
    
    // Show modal
    modal.style.display = 'block';
}

// Open rent vehicle modal
function openRentVehicleModal(vehicle) {
    selectedVehicle = vehicle;
    
    const modal = document.getElementById('rentVehicleModal');
    const title = document.getElementById('rentVehicleTitle');
    const daysInput = document.getElementById('rentalDays');
    const estimatedCost = document.getElementById('estimatedCost');
    
    title.textContent = `Rent ${vehicle.brand} ${vehicle.model}`;
    
    // Reset days input
    daysInput.value = 1;
    
    // Update estimated cost
    estimatedCost.textContent = formatCurrency(vehicle.rentPrice);
    
    // Add event listener to days input for updating the estimated cost
    daysInput.addEventListener('input', function() {
        const days = parseInt(this.value) || 1;
        estimatedCost.textContent = formatCurrency(vehicle.rentPrice * days);
    });
    
    // Show modal
    modal.style.display = 'block';
}

// Initialize add vehicle form
function initAddVehicleForm() {
    const addVehicleBtn = document.getElementById('addVehicleBtn');
    const modal = document.getElementById('addVehicleModal');
    const form = document.getElementById('addVehicleForm');
    const vehicleTypeSelect = document.getElementById('vehicleType');
    
    // Show modal when button is clicked
    addVehicleBtn.addEventListener('click', function() {
        modal.style.display = 'block';
    });
    
    // Show/hide type-specific fields based on vehicle type
    vehicleTypeSelect.addEventListener('change', function() {
        const type = this.value;
        
        // Hide all type-specific fields
        document.querySelectorAll('.type-specific-fields').forEach(field => {
            field.style.display = 'none';
        });
        
        // Show fields for selected type
        if (type === '1') {
            document.getElementById('carFields').style.display = 'block';
        } else if (type === '2') {
            document.getElementById('vanFields').style.display = 'block';
        } else if (type === '3') {
            document.getElementById('bikeFields').style.display = 'block';
        } else if (type === '4') {
            document.getElementById('busFields').style.display = 'block';
        } else if (type === '5') {
            document.getElementById('lorryFields').style.display = 'block';
        }
    });
    
    // Handle form submission
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const vehicleID = document.getElementById('vehicleID').value;
        const vehicleType = document.getElementById('vehicleType').value;
        const brand = document.getElementById('brand').value;
        const model = document.getElementById('model').value;
        const rentPrice = parseFloat(document.getElementById('rentPrice').value);
          // Create vehicle object
        let vehicle = {
            vehicleID: vehicleID,
            brand: brand,
            model: model,
            rentPrice: rentPrice,
            vehicleType: parseInt(vehicleType) // Explicitly store vehicle type
        };
        
        // Add type-specific properties
        if (vehicleType === '1') { // Car
            vehicle.numberOfDoors = parseInt(document.getElementById('numberOfDoors').value);
            vehicle.transmissionType = document.getElementById('transmissionType').value;
        } else if (vehicleType === '2') { // Van
            vehicle.cargoCapacity = parseFloat(document.getElementById('cargoCapacity').value);
        } else if (vehicleType === '3') { // Bike
            vehicle.engineCapacity = parseInt(document.getElementById('engineCapacity').value);
        } else if (vehicleType === '4') { // Bus
            vehicle.seatingCapacity = parseInt(document.getElementById('seatingCapacity').value);
        } else if (vehicleType === '5') { // Lorry
            vehicle.maxLoad = parseFloat(document.getElementById('maxLoad').value);
        }
        
        try {
            // Show loading state
            const submitBtn = form.querySelector('button[type="submit"]');
            submitBtn.textContent = 'Adding...';
            submitBtn.disabled = true;
            
            // Call the API to add vehicle
            const response = await apiRequest('/vehicles/add', 'POST', vehicle);
            
            // Check if image was uploaded
            const imageFile = document.getElementById('vehicleImage').files[0];
            
            if (imageFile) {
                // Create form data for file upload
                const formData = new FormData();
                formData.append('file', imageFile);
                
                // Upload image
                await fetch(`/api/vehicles/uploadImage/${vehicleID}`, {
                    method: 'POST',
                    body: formData
                });
            }
            
            // Reset form
            form.reset();
            
            // Hide modal
            modal.style.display = 'none';
            
            // Reload vehicles
            loadVehicles();
            
        } catch (error) {
            console.error('Error adding vehicle:', error);
            alert('Failed to add vehicle: ' + (error.message || 'Unknown error'));
        } finally {
            // Reset button
            const submitBtn = form.querySelector('button[type="submit"]');
            submitBtn.textContent = 'Add Vehicle';
            submitBtn.disabled = false;
        }
    });
}

// Initialize vehicle details modal
function initVehicleDetailsModal() {
    const rentButton = document.getElementById('rentVehicleBtn');
    const editButton = document.getElementById('editVehicleBtn');
    const deleteButton = document.getElementById('deleteVehicleBtn');
    
    // Rent button
    rentButton.addEventListener('click', function() {
        if (selectedVehicle) {
            openRentVehicleModal(selectedVehicle);
            document.getElementById('vehicleDetailsModal').style.display = 'none';
        }
    });
    
    // Edit button - simplified for this example
    editButton.addEventListener('click', function() {
        if (!selectedVehicle) {
            alert('No vehicle selected');
            return;
        }
        
        // Determine vehicle type and specific properties
        let vehicleType = selectedVehicle.vehicleType || 0;
        if (!vehicleType) {
            // Fallback to identify vehicle type based on properties
            if (selectedVehicle.hasOwnProperty('numberOfDoors')) vehicleType = 1; // Car
            else if (selectedVehicle.hasOwnProperty('cargoCapacity')) vehicleType = 2; // Van
            else if (selectedVehicle.hasOwnProperty('engineCapacity')) vehicleType = 3; // Bike
            else if (selectedVehicle.hasOwnProperty('seatingCapacity')) vehicleType = 4; // Bus
            else if (selectedVehicle.hasOwnProperty('maxLoad')) vehicleType = 5; // Lorry
        }
        
        // Create edit form HTML
        const modalContent = document.createElement('div');
        modalContent.className = 'modal-content';
        modalContent.innerHTML = `
            <span class="close">&times;</span>
            <h2>Edit Vehicle</h2>
            <form id="editVehicleForm">
                <input type="hidden" id="originalVehicleID" value="${selectedVehicle.vehicleID}">
                <div class="form-group">
                    <label for="editVehicleID">Vehicle ID</label>
                    <input type="text" id="editVehicleID" name="vehicleID" value="${selectedVehicle.vehicleID}">
                </div>
                <div class="form-group">
                    <label for="editVehicleType">Vehicle Type</label>
                    <select id="editVehicleType" name="vehicleType" required>
                        <option value="1" ${vehicleType === 1 ? 'selected' : ''}>Car</option>
                        <option value="2" ${vehicleType === 2 ? 'selected' : ''}>Van</option>
                        <option value="3" ${vehicleType === 3 ? 'selected' : ''}>Bike</option>
                        <option value="4" ${vehicleType === 4 ? 'selected' : ''}>Bus</option>
                        <option value="5" ${vehicleType === 5 ? 'selected' : ''}>Lorry</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="editBrand">Brand</label>
                    <input type="text" id="editBrand" name="brand" value="${selectedVehicle.brand}" required>
                </div>
                <div class="form-group">
                    <label for="editModel">Model</label>
                    <input type="text" id="editModel" name="model" value="${selectedVehicle.model}" required>
                </div>
                <div class="form-group">
                    <label for="editRentPrice">Rent Price (per day)</label>
                    <input type="number" id="editRentPrice" name="rentPrice" step="0.01" value="${selectedVehicle.rentPrice}" required>
                </div>
                
                <!-- Car-specific fields -->
                <div class="edit-type-specific-fields" id="editCarFields" style="display: ${vehicleType === 1 ? 'block' : 'none'};">
                    <div class="form-group">
                        <label for="editNumberOfDoors">Number of Doors</label>
                        <input type="number" id="editNumberOfDoors" name="numberOfDoors" min="2" max="5" value="${selectedVehicle.numberOfDoors || 4}">
                    </div>
                    <div class="form-group">
                        <label for="editTransmissionType">Transmission Type</label>
                        <select id="editTransmissionType" name="transmissionType">
                            <option value="Manual" ${selectedVehicle.transmissionType === 'Manual' ? 'selected' : ''}>Manual</option>
                            <option value="Automatic" ${selectedVehicle.transmissionType === 'Automatic' ? 'selected' : ''}>Automatic</option>
                        </select>
                    </div>
                </div>
                
                <!-- Van-specific fields -->
                <div class="edit-type-specific-fields" id="editVanFields" style="display: ${vehicleType === 2 ? 'block' : 'none'};">
                    <div class="form-group">
                        <label for="editCargoCapacity">Cargo Capacity (kg)</label>
                        <input type="number" id="editCargoCapacity" name="cargoCapacity" min="0" value="${selectedVehicle.cargoCapacity || 0}">
                    </div>
                </div>
                
                <!-- Bike-specific fields -->
                <div class="edit-type-specific-fields" id="editBikeFields" style="display: ${vehicleType === 3 ? 'block' : 'none'};">
                    <div class="form-group">
                        <label for="editEngineCapacity">Engine Capacity (cc)</label>
                        <input type="number" id="editEngineCapacity" name="engineCapacity" min="0" value="${selectedVehicle.engineCapacity || 0}">
                    </div>
                </div>
                
                <!-- Bus-specific fields -->
                <div class="edit-type-specific-fields" id="editBusFields" style="display: ${vehicleType === 4 ? 'block' : 'none'};">
                    <div class="form-group">
                        <label for="editSeatingCapacity">Seating Capacity</label>
                        <input type="number" id="editSeatingCapacity" name="seatingCapacity" min="0" value="${selectedVehicle.seatingCapacity || 0}">
                    </div>
                </div>
                
                <!-- Lorry-specific fields -->
                <div class="edit-type-specific-fields" id="editLorryFields" style="display: ${vehicleType === 5 ? 'block' : 'none'};">
                    <div class="form-group">
                        <label for="editMaxLoad">Maximum Load (tons)</label>
                        <input type="number" id="editMaxLoad" name="maxLoad" min="0" step="0.1" value="${selectedVehicle.maxLoad || 0}">
                    </div>
                </div>
                
                <button type="submit" class="btn btn-primary">Update Vehicle</button>
            </form>
        `;
        
        // Create modal
        const editModal = document.createElement('div');
        editModal.className = 'modal';
        editModal.id = 'editVehicleModal';
        editModal.appendChild(modalContent);
        
        // Add modal to document
        document.body.appendChild(editModal);
        
        // Show modal
        editModal.style.display = 'block';
        
        // Close button functionality
        const closeButton = modalContent.querySelector('.close');
        closeButton.addEventListener('click', function() {
            editModal.style.display = 'none';
            document.body.removeChild(editModal);
        });
        
        // Show/hide type-specific fields based on vehicle type
        const vehicleTypeSelect = document.getElementById('editVehicleType');
        vehicleTypeSelect.addEventListener('change', function() {
            const type = this.value;
            
            // Hide all type-specific fields
            document.querySelectorAll('.edit-type-specific-fields').forEach(field => {
                field.style.display = 'none';
            });
            
            // Show fields for selected type
            if (type === '1') {
                document.getElementById('editCarFields').style.display = 'block';
            } else if (type === '2') {
                document.getElementById('editVanFields').style.display = 'block';
            } else if (type === '3') {
                document.getElementById('editBikeFields').style.display = 'block';
            } else if (type === '4') {
                document.getElementById('editBusFields').style.display = 'block';
            } else if (type === '5') {
                document.getElementById('editLorryFields').style.display = 'block';
            }
        });
        
        // Form submission
        const editForm = document.getElementById('editVehicleForm');
        editForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            try {
                // Show loading state
                const submitBtn = editForm.querySelector('button[type="submit"]');
                submitBtn.textContent = 'Updating...';
                submitBtn.disabled = true;
                
                // Get form data
                const vehicleType = parseInt(document.getElementById('editVehicleType').value);
                const originalVehicleID = document.getElementById('originalVehicleID').value;
                const newVehicleID = document.getElementById('editVehicleID').value;
                
                // Create base vehicle object with common properties
                const updatedVehicle = {
                    vehicleID: newVehicleID,
                    originalVehicleID: originalVehicleID,
                    brand: document.getElementById('editBrand').value,
                    model: document.getElementById('editModel').value,
                    rentPrice: parseFloat(document.getElementById('editRentPrice').value),
                    vehicleType: vehicleType,
                    // Preserve image path
                    imagePath: selectedVehicle.imagePath || ''
                };
                
                // Add type-specific properties based on selected vehicle type
                if (vehicleType === 1) { // Car
                    updatedVehicle.numberOfDoors = parseInt(document.getElementById('editNumberOfDoors').value) || 4;
                    updatedVehicle.transmissionType = document.getElementById('editTransmissionType').value;
                } else if (vehicleType === 2) { // Van
                    updatedVehicle.cargoCapacity = parseFloat(document.getElementById('editCargoCapacity').value) || 0;
                } else if (vehicleType === 3) { // Bike
                    updatedVehicle.engineCapacity = parseInt(document.getElementById('editEngineCapacity').value) || 0;
                } else if (vehicleType === 4) { // Bus
                    updatedVehicle.seatingCapacity = parseInt(document.getElementById('editSeatingCapacity').value) || 0;
                } else if (vehicleType === 5) { // Lorry
                    updatedVehicle.maxLoad = parseFloat(document.getElementById('editMaxLoad').value) || 0;
                }
                
                // Call API to update vehicle
                await apiRequest('/vehicles/update', 'PUT', updatedVehicle);
                
                // Close modal
                editModal.style.display = 'none';
                document.body.removeChild(editModal);
                
                // Close details modal
                document.getElementById('vehicleDetailsModal').style.display = 'none';
                
                // Reload vehicles to show updated data
                await loadVehicles();
                
                // Show success message using the new function
                if (typeof showSuccessMessage === 'function') {
                    showSuccessMessage('Vehicle updated successfully!');
                } else {
                    alert('Vehicle updated successfully!');
                }
                
            } catch (error) {
                console.error('Error updating vehicle:', error);
                alert('Failed to update vehicle: ' + (error.message || 'Unknown error'));
            } finally {
                // Reset button (in case the modal isn't closed)
                const submitBtn = editForm.querySelector('button[type="submit"]');
                submitBtn.textContent = 'Update Vehicle';
                submitBtn.disabled = false;
            }
        });
        
        // Hide details modal
        document.getElementById('vehicleDetailsModal').style.display = 'none';
    });
    
    // Delete button
    deleteButton.addEventListener('click', async function() {
        if (selectedVehicle && confirm('Are you sure you want to delete this vehicle?')) {
            try {
                await apiRequest(`/vehicles/delete/${selectedVehicle.vehicleID}`, 'DELETE');
                
                // Hide modal
                document.getElementById('vehicleDetailsModal').style.display = 'none';
                
                // Reload vehicles
                loadVehicles();
                
            } catch (error) {
                console.error('Error deleting vehicle:', error);
                alert('Failed to delete vehicle: ' + (error.message || 'Unknown error'));
            }
        }
    });
}

// Initialize rent vehicle modal
function initRentVehicleModal() {
    const form = document.getElementById('rentVehicleForm');
    
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        if (!selectedVehicle) {
            console.error('No vehicle selected');
            alert('No vehicle selected');
            return;
        }
          const customer = JSON.parse(localStorage.getItem('customer'));
        if (!customer) {
            console.error('No customer logged in');
            alert('Please log in again');
            window.location.href = 'login.html';
            return;
        }
        
        // Debug logging
        console.log('Customer in localStorage:', customer);
        console.log('Customer ID type:', typeof customer.customerId);
        
        const days = parseInt(document.getElementById('rentalDays').value) || 1;
        
        // Ensure we have a valid vehicle ID
        if (!selectedVehicle.vehicleID) {
            console.error('Selected vehicle has no ID');
            alert('Invalid vehicle data. Please try selecting a different vehicle.');
            return;
        }
          console.log('Attempting to rent vehicle:', {
            customerId: customer.customerId,
            vehicleId: selectedVehicle.vehicleID,
            days: days,
            selectedVehicle: selectedVehicle,
            customerObj: customer // Log the entire customer object to debug
        });          try {
            // Show loading state
            const submitBtn = form.querySelector('button[type="submit"]');
            submitBtn.textContent = 'Confirming...';
            submitBtn.disabled = true;            
              // Ensure customerId is a number and format it correctly
            let customerId = customer.customerId;
            console.log('Customer ID before request:', customerId, 'Type:', typeof customerId);
            
            // If customerId is a string but contains a number, convert it
            if (typeof customerId === 'string' && !isNaN(parseInt(customerId))) {
                customerId = parseInt(customerId);
            }
            
            // Create a temporary object from the customer data but with an ID that works
            const tempCustomerData = { ...customer };
              // Try to use the customer data to register a new customer or find an existing one
            try {
                const registerResponse = await apiRequest('/customers/register', 'POST', {
                    name: customer.name || "Guest User",
                    email: customer.email,
                    password: customer.password || "password123", // Fallback password, should be changed
                    contactNumber: customer.contactNumber || "0000000000",
                    driverLicenseNumber: customer.driverLicenseNumber || 12345
                });
                
                console.log('Register response:', registerResponse);
                if (registerResponse && registerResponse.customer) {
                    // Update localStorage with new customer data
                    localStorage.setItem('customer', JSON.stringify(registerResponse.customer));
                    customerId = registerResponse.customer.customerId;
                }
            } catch (regError) {
                console.log('Registration attempt error (may be because user exists):', regError);
                
                // Try to log in instead
                try {
                    const loginResponse = await apiRequest('/customers/login', 'POST', {
                        email: customer.email,
                        password: customer.password || "password123"  // Use the same fallback
                    });
                    
                    console.log('Login response:', loginResponse);
                    if (loginResponse && loginResponse.customer) {
                        // Update localStorage with retrieved customer data
                        localStorage.setItem('customer', JSON.stringify(loginResponse.customer));
                        customerId = loginResponse.customer.customerId;
                    }
                } catch (loginError) {
                    console.error('Login error:', loginError);
                    // Try with the test user as a last resort
                    try {
                        const testLoginResponse = await apiRequest('/customers/login', 'POST', {
                            email: "test@example.com",
                            password: "password123"
                        });
                        
                        console.log('Test login response:', testLoginResponse);
                        if (testLoginResponse && testLoginResponse.customer) {
                            // Update localStorage with test customer data
                            localStorage.setItem('customer', JSON.stringify(testLoginResponse.customer));
                            customerId = testLoginResponse.customer.customerId;
                            alert('Using test account for rental. Please log in again after completing this operation.');
                        } else {
                            alert('Failed to authenticate. Please log in again.');
                            window.location.href = 'login.html';
                            return;
                        }
                    } catch (testLoginError) {
                        console.error('Test login error:', testLoginError);
                        alert('Failed to authenticate. Please log in again.');
                        window.location.href = 'login.html';
                        return;
                    }
                }
            }
            
            const url = `/api/customers/${encodeURIComponent(customerId)}/rent/${encodeURIComponent(selectedVehicle.vehicleID)}?days=${days}`;
            console.log('Direct rental request URL:', url);
            
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            console.log('Raw response status:', response.status, response.statusText);
            
            if (!response.ok) {
                let errorMessage = `Request failed: ${response.status} ${response.statusText}`;
                try {
                    const errorText = await response.text();
                    console.error('Error response body:', errorText);
                    
                    if (errorText && errorText.length > 0) {
                        try {
                            const errorJson = JSON.parse(errorText);
                            if (errorJson && errorJson.message) {
                                errorMessage = errorJson.message;
                            }
                        } catch (jsonError) {
                            console.log('Error parsing error response as JSON', jsonError);
                        }
                    }
                } catch (readError) {
                    console.error('Error reading error response:', readError);
                }
                
                throw new Error(errorMessage);
            }
            
            let data;
            try {
                const responseText = await response.text();
                console.log('Raw response text:', responseText);
                
                // Only try to parse as JSON if there's content
                if (responseText && responseText.trim().length > 0) {
                    data = JSON.parse(responseText);
                    console.log('Rental response data:', data);
                }
            } catch (jsonError) {
                console.error('Error parsing response JSON:', jsonError);
                data = { message: 'Rental successful, but response data could not be parsed.' };
            }
              // Reset form
            form.reset();
            
            // Hide modal
            document.getElementById('rentVehicleModal').style.display = 'none';
            
            // Check if we have a purchase ID in the response
            if (data && data.purchaseId) {
                // Redirect to the payment page with the purchase ID
                window.location.href = `payment.html?purchaseId=${data.purchaseId}`;
            } else {                // Show success message without redirect
                alert(`You have successfully rented the ${selectedVehicle.brand} ${selectedVehicle.model} for ${days} days.`);
                
                // Remove the rented vehicle from the vehicles array
                vehicles = vehicles.filter(v => v.vehicleID !== selectedVehicle.vehicleID);
                
                // Reload vehicles to update the UI
                renderVehicles(vehicles);
                
                // Also reload the rented vehicles list
                loadRentedVehicles();
            }
            
        } catch (error) {
            console.error('Error renting vehicle:', error);
            alert('Failed to rent vehicle: ' + (error.message || 'Unknown error'));
        } finally {
            // Reset button
            const submitBtn = form.querySelector('button[type="submit"]');
            submitBtn.textContent = 'Confirm Rental';
            submitBtn.disabled = false;
        }
    });
}
