// Admin Bookings Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const bookingsTableBody = document.getElementById('bookingsTableBody');
    const emptyBookings = document.getElementById('emptyBookings');
    const eventTypeFilter = document.getElementById('eventTypeFilter');
    const statusFilter = document.getElementById('statusFilter');
    const startDateFilter = document.getElementById('startDateFilter');
    const endDateFilter = document.getElementById('endDateFilter');
    const applyFiltersBtn = document.getElementById('applyFiltersBtn');
    
    // Booking modal
    const bookingModal = document.getElementById('bookingModal');
    const modalClose = bookingModal.querySelector('.close');
    const bookingAdminActions = document.getElementById('bookingAdminActions');
    const statusButtons = bookingAdminActions.querySelectorAll('button');
    
    // Modal elements
    const modalBookingTitle = document.getElementById('modalBookingTitle');
    const modalBookingId = document.getElementById('modalBookingId');
    const modalEventName = document.getElementById('modalEventName');
    const modalEventImage = document.getElementById('modalEventImage');
    const modalEventType = document.getElementById('modalEventType');
    const modalCustomerName = document.getElementById('modalCustomerName');
    const modalCustomerContact = document.getElementById('modalCustomerContact');
    const modalBookingDate = document.getElementById('modalBookingDate');
    const modalEventDate = document.getElementById('modalEventDate');
    const modalBookingStatus = document.getElementById('modalBookingStatus');
    const modalTotalPrice = document.getElementById('modalTotalPrice');
    const modalSpecialRequirements = document.getElementById('modalSpecialRequirements');
    const modalEventVehicles = document.getElementById('modalEventVehicles');
    
    // Check if admin is logged in
    checkAdminStatus();
    
    // Set default date filters (last 30 days to next 30 days)
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);
    const thirtyDaysFromNow = new Date();
    thirtyDaysFromNow.setDate(today.getDate() + 30);
    
    startDateFilter.value = formatDateForInput(thirtyDaysAgo);
    endDateFilter.value = formatDateForInput(thirtyDaysFromNow);
    
    // Load bookings
    loadBookings();
    
    // Event listeners
    applyFiltersBtn.addEventListener('click', loadBookings);
    modalClose.addEventListener('click', closeModal);
    
    statusButtons.forEach(button => {
        button.addEventListener('click', function() {
            const status = this.dataset.status;
            changeBookingStatus(status);
        });
    });
    
    window.addEventListener('click', function(event) {
        if (event.target === bookingModal) {
            closeModal();
        }
    });
    
    // Functions
    function loadBookings() {
        bookingsTableBody.innerHTML = `
            <tr>
                <td colspan="9" class="loading-cell">
                    <i class="fas fa-spinner fa-spin"></i> Loading bookings...
                </td>
            </tr>
        `;
        
        // First, get all bookings
        fetch('/api/event-bookings')
            .then(response => response.json())
            .then(bookings => {
                // Apply filters
                const filteredBookings = applyFilters(bookings);
                
                if (filteredBookings.length === 0) {
                    bookingsTableBody.innerHTML = '';
                    emptyBookings.style.display = 'flex';
                } else {
                    bookingsTableBody.innerHTML = '';
                    emptyBookings.style.display = 'none';
                    
                    filteredBookings.forEach(booking => {
                        const row = createBookingRow(booking);
                        bookingsTableBody.appendChild(row);
                    });
                }
            })
            .catch(error => {
                console.error('Error loading bookings:', error);
                bookingsTableBody.innerHTML = `
                    <tr>
                        <td colspan="9" class="error-cell">
                            <i class="fas fa-exclamation-circle"></i> 
                            Failed to load bookings. Please try again later.
                        </td>
                    </tr>
                `;
            });
    }
    
    function applyFilters(bookings) {
        const selectedType = eventTypeFilter.value;
        const selectedStatus = statusFilter.value;
        const startDate = startDateFilter.value ? new Date(startDateFilter.value) : null;
        const endDate = endDateFilter.value ? new Date(endDateFilter.value) : null;
        
        return bookings.filter(booking => {
            const event = booking.event;
            const eventDate = new Date(booking.eventDate);
            
            // Event type filter
            if (selectedType !== 'all' && event.eventType !== selectedType) {
                return false;
            }
            
            // Status filter
            if (selectedStatus !== 'all' && booking.status !== selectedStatus) {
                return false;
            }
            
            // Date range filter
            if (startDate && eventDate < startDate) {
                return false;
            }
            
            if (endDate) {
                // Set the end date to the end of the day
                const endOfDay = new Date(endDate);
                endOfDay.setHours(23, 59, 59, 999);
                
                if (eventDate > endOfDay) {
                    return false;
                }
            }
            
            return true;
        });
    }
    
    function createBookingRow(booking) {
        const row = document.createElement('tr');
        
        const event = booking.event;
        const eventDate = new Date(booking.eventDate);
        const bookingDate = new Date(booking.bookingDate);
        
        row.innerHTML = `
            <td>${booking.bookingId}</td>
            <td>${booking.customerName}</td>
            <td>${event.name}</td>
            <td>${formatEventType(event.eventType)}</td>
            <td>${formatDate(bookingDate)}</td>
            <td>${formatDateTime(eventDate)}</td>
            <td>$${booking.totalPrice.toFixed(2)}</td>
            <td>
                <span class="status-badge ${booking.status.toLowerCase()}">
                    ${booking.status}
                </span>
            </td>
            <td class="actions-cell">
                <button class="btn-icon view-btn" title="View Details">
                    <i class="fas fa-eye"></i>
                </button>
                ${getStatusActionButtons(booking.status)}
            </td>
        `;
        
        // Add event listener for view button
        row.querySelector('.view-btn').addEventListener('click', () => showBookingDetails(booking));
        
        // Add event listeners for status buttons
        const confirmBtn = row.querySelector('.confirm-btn');
        if (confirmBtn) {
            confirmBtn.addEventListener('click', () => quickChangeStatus(booking.id, 'CONFIRMED'));
        }
        
        const completeBtn = row.querySelector('.complete-btn');
        if (completeBtn) {
            completeBtn.addEventListener('click', () => quickChangeStatus(booking.id, 'COMPLETED'));
        }
        
        const cancelBtn = row.querySelector('.cancel-btn');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => quickChangeStatus(booking.id, 'CANCELLED'));
        }
        
        return row;
    }
    
    function getStatusActionButtons(status) {
        // Return different action buttons based on the current status
        switch (status) {
            case 'PENDING':
                return `
                    <button class="btn-icon confirm-btn" title="Confirm Booking">
                        <i class="fas fa-check-circle"></i>
                    </button>
                    <button class="btn-icon cancel-btn" title="Cancel Booking">
                        <i class="fas fa-times-circle"></i>
                    </button>
                `;
            case 'CONFIRMED':
                return `
                    <button class="btn-icon complete-btn" title="Mark as Completed">
                        <i class="fas fa-clipboard-check"></i>
                    </button>
                    <button class="btn-icon cancel-btn" title="Cancel Booking">
                        <i class="fas fa-times-circle"></i>
                    </button>
                `;
            case 'COMPLETED':
            case 'CANCELLED':
                return ''; // No actions for completed or cancelled bookings
            default:
                return '';
        }
    }
    
    function showBookingDetails(booking) {
        const event = booking.event;
        const eventDate = new Date(booking.eventDate);
        const bookingDate = new Date(booking.bookingDate);
        
        // Set modal content
        modalBookingTitle.textContent = `Booking: ${event.name}`;
        modalBookingId.textContent = booking.bookingId;
        modalEventName.textContent = event.name;
        modalEventImage.src = event.imagePath || 'images/event-placeholder.jpg';
        modalEventType.textContent = formatEventType(event.eventType);
        modalCustomerName.textContent = booking.customerName;
        modalCustomerContact.textContent = booking.contactNumber;
        modalBookingDate.textContent = formatDateTime(bookingDate);
        modalEventDate.textContent = formatDateTime(eventDate);
        modalBookingStatus.textContent = booking.status;
        modalBookingStatus.className = booking.status.toLowerCase();
        modalTotalPrice.textContent = booking.totalPrice.toFixed(2);
        modalSpecialRequirements.textContent = booking.specialRequirements || 'None';
        
        // Set booking ID for status change buttons
        bookingAdminActions.dataset.bookingId = booking.id;
        
        // Update status buttons based on current status
        updateStatusButtons(booking.status);
        
        // Load vehicles for this event
        loadEventVehicles(event.id);
        
        // Show modal
        bookingModal.style.display = 'block';
    }
    
    function updateStatusButtons(currentStatus) {
        // Show/hide status buttons based on current status
        statusButtons.forEach(button => {
            const status = button.dataset.status;
            
            if (currentStatus === status) {
                // Can't change to the same status
                button.style.display = 'none';
            } else if (currentStatus === 'COMPLETED' || currentStatus === 'CANCELLED') {
                // Can't change status of completed or cancelled bookings
                button.style.display = 'none';
            } else if (status === 'CONFIRMED' && currentStatus !== 'PENDING') {
                // Can only confirm pending bookings
                button.style.display = 'none';
            } else {
                button.style.display = 'inline-block';
            }
        });
        
        // Show/hide the entire actions section
        if (currentStatus === 'COMPLETED' || currentStatus === 'CANCELLED') {
            bookingAdminActions.style.display = 'none';
        } else {
            bookingAdminActions.style.display = 'block';
        }
    }
    
    function loadEventVehicles(eventId) {
        modalEventVehicles.innerHTML = `
            <div class="loading">
                <i class="fas fa-spinner fa-spin"></i> Loading vehicles...
            </div>
        `;
        
        fetch(`/api/events/${eventId}/vehicles`)
            .then(response => response.json())
            .then(vehicles => {
                modalEventVehicles.innerHTML = '';
                
                if (vehicles.length === 0) {
                    modalEventVehicles.innerHTML = `
                        <p class="no-vehicles-message">No vehicles assigned to this event.</p>
                    `;
                    return;
                }
                
                vehicles.forEach(vehicle => {
                    const vehicleCard = document.createElement('div');
                    vehicleCard.className = 'vehicle-card small';
                    
                    const imagePath = vehicle.imagePath || 'images/car-placeholder.jpg';
                    
                    vehicleCard.innerHTML = `
                        <div class="vehicle-image">
                            <img src="${imagePath}" alt="${vehicle.brand} ${vehicle.model}">
                        </div>
                        <div class="vehicle-content">
                            <h4>${vehicle.brand} ${vehicle.model}</h4>
                            <p>${getVehicleType(vehicle)}</p>
                        </div>
                    `;
                    
                    modalEventVehicles.appendChild(vehicleCard);
                });
            })
            .catch(error => {
                console.error('Error loading vehicles:', error);
                modalEventVehicles.innerHTML = `
                    <div class="error-message">
                        <i class="fas fa-exclamation-circle"></i> 
                        Failed to load vehicles. Please try again later.
                    </div>
                `;
            });
    }
    
    function changeBookingStatus(status) {
        const bookingId = bookingAdminActions.dataset.bookingId;
        
        if (!confirm(`Are you sure you want to change the booking status to ${status}?`)) {
            return;
        }
        
        fetch(`/api/event-bookings/${bookingId}/status/${status}`, {
            method: 'PUT'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Status change failed');
            }
            return response.json();
        })
        .then(booking => {
            alert(`Booking status changed to ${status} successfully.`);
            closeModal();
            loadBookings();
        })
        .catch(error => {
            console.error('Error changing booking status:', error);
            alert('Failed to change booking status. Please try again later.');
        });
    }
    
    function quickChangeStatus(bookingId, status) {
        if (!confirm(`Are you sure you want to change the booking status to ${status}?`)) {
            return;
        }
        
        fetch(`/api/event-bookings/${bookingId}/status/${status}`, {
            method: 'PUT'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Status change failed');
            }
            return response.json();
        })
        .then(booking => {
            alert(`Booking status changed to ${status} successfully.`);
            loadBookings();
        })
        .catch(error => {
            console.error('Error changing booking status:', error);
            alert('Failed to change booking status. Please try again later.');
        });
    }
    
    function closeModal() {
        bookingModal.style.display = 'none';
    }
    
    // Helper functions
    function formatEventType(type) {
        if (!type) return 'Unknown';
        
        return type
            .replace('_', ' ')
            .split(' ')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
            .join(' ');
    }
    
    function formatDate(date) {
        return new Date(date).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }
    
    function formatDateTime(date) {
        return new Date(date).toLocaleString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
    
    function formatDateForInput(date) {
        return date.toISOString().split('T')[0];
    }
    
    function getVehicleType(vehicle) {
        if (vehicle.discriminator) {
            return vehicle.discriminator.charAt(0).toUpperCase() + 
                   vehicle.discriminator.slice(1).toLowerCase();
        }
        
        // Determine by checking specific properties
        if (vehicle.hasOwnProperty('numberOfDoors')) return 'Car';
        if (vehicle.hasOwnProperty('cargoCapacity')) return 'Lorry';
        if (vehicle.hasOwnProperty('seatCapacity')) return 'Bus';
        if (vehicle.hasOwnProperty('engineType')) return 'Bike';
        if (vehicle.hasOwnProperty('cargoSpace')) return 'Van';
        
        return 'Vehicle';
    }
    
    function checkAdminStatus() {
        // This should be replaced with your actual admin authentication logic
        // For now, we're just checking if a user is logged in
        const user = localStorage.getItem('user');
        if (!user) {
            window.location.href = 'login.html';
        }
        
        // Check if the user has admin role (you would need to implement this)
        // const userData = JSON.parse(user);
        // if (!userData.isAdmin) {
        //     window.location.href = 'index.html';
        // }
    }
});
