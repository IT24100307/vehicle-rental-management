// Rentals page functionality

// Global variable to track the currently selected rental
let selectedRental = null;

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in
    const customer = checkAuth();
    if (!customer) return;
    
    // Load customer rentals
    loadCustomerRentals();
    
    // Initialize rental details modal
    initRentalDetailsModal();
});

// Load customer rentals
async function loadCustomerRentals() {
    const customer = JSON.parse(localStorage.getItem('customer'));
    if (!customer) return;
    
    try {
        const activeRentalsList = document.getElementById('activeRentalsList');
        const rentalHistoryList = document.getElementById('rentalHistoryList');
        const emptyActiveRentals = document.getElementById('emptyActiveRentals');
        const emptyRentalHistory = document.getElementById('emptyRentalHistory');
        
        activeRentalsList.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading your rentals...</div>';
        rentalHistoryList.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading your rental history...</div>';
        
        // Call the API to get customer rentals (both active and history)
        const rentals = await apiRequest(`/customers/${customer.customerId}/rentals`);
        
        // Separate active rentals and rental history
        const activeRentals = rentals.filter(rental => rental.rented !== false);
        const rentalHistory = rentals.filter(rental => rental.rented === false);
        
        // Handle active rentals display
        if (!activeRentals || activeRentals.length === 0) {
            activeRentalsList.style.display = 'none';
            emptyActiveRentals.style.display = 'block';
        } else {
            activeRentalsList.style.display = 'block';
            emptyActiveRentals.style.display = 'none';
            
            // Clear list
            activeRentalsList.innerHTML = '';
            
            // Add each active rental to list
            activeRentals.forEach(rental => {
                const rentalItem = createRentalItem(rental);
                activeRentalsList.appendChild(rentalItem);
            });
        }
        
        // Handle rental history display
        if (!rentalHistory || rentalHistory.length === 0) {
            rentalHistoryList.style.display = 'none';
            emptyRentalHistory.style.display = 'block';
        } else {
            rentalHistoryList.style.display = 'block';
            emptyRentalHistory.style.display = 'none';
            
            // Clear list
            rentalHistoryList.innerHTML = '';
            
            // Add each returned rental to history list
            rentalHistory.forEach(rental => {
                const rentalItem = createRentalItem(rental);
                rentalHistoryList.appendChild(rentalItem);
            });
        }
    } catch (error) {
        console.error('Error loading rentals:', error);
        const activeRentalsList = document.getElementById('activeRentalsList');
        const rentalHistoryList = document.getElementById('rentalHistoryList');
        
        activeRentalsList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-circle fa-3x"></i>
                <h2>Error Loading Rentals</h2>
                <p>${error.message || 'Failed to load rentals. Please try again.'}</p>
            </div>
        `;
        rentalHistoryList.innerHTML = '';
    }
}

// Create a rental item element
function createRentalItem(rental) {
    const vehicle = rental.vehicle || {};
    
    // Handle potentially null or missing vehicle data
    const brand = vehicle.brand || 'Unknown';
    const model = vehicle.model || 'Vehicle';
    const price = vehicle.rentPrice || 0;
    const imagePath = vehicle.imagePath || 'https://via.placeholder.com/150x100?text=No+Image';
    const vehicleId = vehicle.vehicleID || 'Unknown';
    
    // Determine rental status (active or returned)
    const isActive = rental.rented !== false; // If rented property is missing, assume it's active
    const statusClass = isActive ? 'status-active' : 'status-returned';
    const statusText = isActive ? 'Active' : 'Returned';
    
    const rentalItem = document.createElement('div');
    rentalItem.className = 'rental-item';
    rentalItem.innerHTML = `
        <img src="${imagePath}" alt="${brand} ${model}" class="rental-image">
        <div class="rental-details">
            <h3 class="rental-title">${brand} ${model}</h3>
            <div class="rental-info">
                <div><strong>Rental ID:</strong> ${rental.purchaseId}</div>
                <div><strong>Rental Date:</strong> ${formatDate(rental.purchaseDate)}</div>
                <div><strong>Duration:</strong> ${rental.rentalDays} days</div>
                <div><strong>Daily Rate:</strong> ${formatCurrency(price)}</div>
                <div><strong>Total Cost:</strong> ${formatCurrency(price * rental.rentalDays)}</div>
            </div>
            <span class="rental-status ${statusClass}">${statusText}</span>
            <div class="rental-actions">
                ${isActive ? `
                <button class="btn btn-primary view-rental-btn" data-id="${rental.purchaseId}">View Details</button>
                <button class="btn btn-secondary pay-rental-btn" data-id="${rental.purchaseId}">Process Payment</button>
                ` : `
                <button class="btn btn-secondary view-rental-btn" data-id="${rental.purchaseId}">View Details</button>
                `}
            </div>
        </div>
    `;
    
    // Add event listeners
    const viewDetailsBtn = rentalItem.querySelector('.view-rental-btn');
    viewDetailsBtn.addEventListener('click', () => openRentalDetailsModal(rental));
    
    // Add payment button event listener if active rental
    if (isActive) {
        const payBtn = rentalItem.querySelector('.pay-rental-btn');
        payBtn.addEventListener('click', () => {
            window.location.href = `payment.html?purchaseId=${rental.purchaseId}`;
        });
    }
    
    return rentalItem;
}

// Open rental details modal
function openRentalDetailsModal(rental) {
    // Store the selected rental globally
    selectedRental = rental;
    
    const modal = document.getElementById('rentalDetailsModal');
    const title = document.getElementById('rentalDetailsTitle');
    const body = document.getElementById('rentalDetailsBody');
    const returnButton = document.getElementById('returnVehicleBtn');
    const processPaymentButton = document.getElementById('processPaymentBtn');
    
    const vehicle = rental.vehicle || {};
    
    // Handle potentially null or missing vehicle data
    const brand = vehicle.brand || 'Unknown';
    const model = vehicle.model || 'Vehicle';
    const price = vehicle.rentPrice || 0;
    const imagePath = vehicle.imagePath || 'https://via.placeholder.com/400x250?text=No+Image';
    const vehicleId = vehicle.vehicleID || 'Unknown';
    
    // Determine if the rental is active or returned
    const isActive = rental.rented !== false;
    const statusClass = isActive ? 'status-active' : 'status-returned';
    const statusText = isActive ? 'Active' : 'Returned';
    
    // Show or hide action buttons based on rental status
    returnButton.style.display = isActive ? 'inline-block' : 'none';
    processPaymentButton.style.display = isActive ? 'inline-block' : 'none';
    
    title.textContent = `${brand} ${model} Rental Details`;
    
    // Determine vehicle type
    let vehicleType = 'Standard Vehicle';
    
    if (vehicle.hasOwnProperty('numberOfDoors')) vehicleType = 'Car';
    else if (vehicle.hasOwnProperty('cargoCapacity')) vehicleType = 'Van';
    else if (vehicle.hasOwnProperty('engineCapacity')) vehicleType = 'Bike';
    else if (vehicle.hasOwnProperty('seatingCapacity')) vehicleType = 'Bus';
    else if (vehicle.hasOwnProperty('maxLoad')) vehicleType = 'Lorry';
    
    // Build body content
    body.innerHTML = `
        <div class="rental-details-content">
            <img src="${imagePath}" alt="${brand} ${model}" class="vehicle-detail-img">
            
            <div class="rental-info-section">
                <h3>Rental Information</h3>
                <div><strong>Rental ID:</strong> ${rental.purchaseId}</div>
                <div><strong>Rental Date:</strong> ${formatDate(rental.purchaseDate)}</div>
                <div><strong>Duration:</strong> ${rental.rentalDays} days</div>
                <div><strong>Daily Rate:</strong> ${formatCurrency(price)}</div>
                <div><strong>Total Cost:</strong> ${formatCurrency(price * rental.rentalDays)}</div>
                <div><strong>Status:</strong> <span class="rental-status ${statusClass}">${statusText}</span></div>
            </div>
            
            <div class="rental-info-section">
                <h3>Vehicle Information</h3>
                <div><strong>ID:</strong> ${vehicleId}</div>
                <div><strong>Type:</strong> ${vehicleType}</div>
                <div><strong>Brand:</strong> ${brand}</div>
                <div><strong>Model:</strong> ${model}</div>
            </div>
        </div>
    `;
    
    // Show modal
    modal.style.display = 'block';
}

// Initialize rental details modal
function initRentalDetailsModal() {
    const returnButton = document.getElementById('returnVehicleBtn');
    const printReceiptButton = document.getElementById('printReceiptBtn');
    const processPaymentButton = document.getElementById('processPaymentBtn');
    const modal = document.getElementById('rentalDetailsModal');
    const closeBtn = modal.querySelector('.close');
    
    // Close button functionality
    closeBtn.addEventListener('click', function() {
        modal.style.display = 'none';
    });
    
    // Return vehicle button
    returnButton.addEventListener('click', async function() {
        if (!selectedRental || !selectedRental.purchaseId) {
            alert('Error: No rental selected');
            return;
        }
        
        // Check if the rental is active before allowing return
        if (selectedRental.rented === false) {
            alert('This vehicle has already been returned.');
            return;
        }
        
        if (!confirm(`Are you sure you want to return this vehicle? (${selectedRental.vehicle.brand} ${selectedRental.vehicle.model})`)) {
            return;
        }
        
        try {
            // Show loading state
            returnButton.textContent = 'Processing...';
            returnButton.disabled = true;
            
            // Call the API to return the vehicle
            const response = await fetch(`/customers/rental/${selectedRental.purchaseId}/return`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Request failed: ${response.status} ${response.statusText}`);
            }
            
            const data = await response.json();
            
            // Close modal
            document.getElementById('rentalDetailsModal').style.display = 'none';
            
            // Show success message
            alert('Vehicle returned successfully.');
            
            // Force refresh of customer data from server to get updated rentals list
            try {
                const customer = JSON.parse(localStorage.getItem('customer'));
                if (customer) {
                    // Get fresh customer data
                    const freshCustomerData = await apiRequest(`/customers/${customer.customerId}`);
                    if (freshCustomerData) {
                        localStorage.setItem('customer', JSON.stringify(freshCustomerData));
                        console.log('Customer data refreshed');
                    }
                }
            } catch (customerRefreshError) {
                console.error('Failed to refresh customer data:', customerRefreshError);
            }
            
            // Reload the customer rentals
            loadCustomerRentals();
            
            // If we're on the inventory page, refresh the vehicle list
            if (typeof loadVehicles === 'function') {
                try {
                    loadVehicles();
                    console.log('Vehicle inventory refreshed');
                } catch (refreshError) {
                    console.log('Could not refresh inventory (probably not on inventory page)', refreshError);
                }
            }
            
        } catch (error) {
            console.error('Error returning vehicle:', error);
            alert('Failed to return vehicle: ' + (error.message || 'Unknown error'));
        } finally {
            // Reset button
            returnButton.textContent = 'Return Vehicle';
            returnButton.disabled = false;
        }
    });
    
    // Print receipt button - simplified for this example
    printReceiptButton.addEventListener('click', function() {
        window.print();
    });
    
    // Process payment button
    processPaymentButton.addEventListener('click', function() {
        // Get the current rental ID from the modal title
        const title = document.getElementById('rentalDetailsTitle').textContent;
        const match = title.match(/Details$/);
        
        if (match && selectedRental) {
            window.location.href = `payment.html?purchaseId=${selectedRental.purchaseId}`;
        } else {
            alert('Error: Could not determine rental ID');
        }
    });
}
