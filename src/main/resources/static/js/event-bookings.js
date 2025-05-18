document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const bookingsList = document.getElementById('bookingsList');
    const emptyBookings = document.getElementById('emptyBookings');
    const bookingStatusFilter = document.getElementById('bookingStatusFilter');
    const bookingDateFilter = document.getElementById('bookingDateFilter');
    const bookingDetailsModal = document.getElementById('bookingDetailsModal');
    const confirmCancelModal = document.getElementById('confirmCancelModal');
    const bookingEventName = document.getElementById('bookingEventName');
    const bookingId = document.getElementById('bookingId');
    const bookingDate = document.getElementById('bookingDate');
    const bookingStatus = document.getElementById('bookingStatus');
    const bookingPrice = document.getElementById('bookingPrice');
    const bookingRequests = document.getElementById('bookingRequests');
    const eventImage = document.getElementById('eventImage');
    const eventType = document.getElementById('eventType');
    const eventDuration = document.getElementById('eventDuration');
    const eventDescription = document.getElementById('eventDescription');
    const eventVehiclesContainer = document.getElementById('eventVehiclesContainer');
    const cancelBookingBtn = document.getElementById('cancelBookingBtn');
    const closeModalBtn = document.getElementById('closeModalBtn');
    const cancelEventName = document.getElementById('cancelEventName');
    const confirmCancelBtn = document.getElementById('confirmCancelBtn');
    const cancelCancelBtn = document.getElementById('cancelCancelBtn');

    // State
    let bookings = [];
    let currentBooking = null;

    // Fetch bookings on page load
    fetchBookings();

    // Event listeners
    bookingStatusFilter.addEventListener('change', filterBookings);
    bookingDateFilter.addEventListener('change', filterBookings);
    cancelBookingBtn.addEventListener('click', openCancelModal);
    closeModalBtn.addEventListener('click', () => bookingDetailsModal.style.display = 'none');
    confirmCancelBtn.addEventListener('click', cancelBooking);
    cancelCancelBtn.addEventListener('click', () => confirmCancelModal.style.display = 'none');

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
    function fetchBookings() {
        // Show loading state
        bookingsList.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading your bookings...</div>';
        
        // Fetch bookings from the API
        fetch('/api/event-bookings/customer')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                bookings = data;
                renderBookings(bookings);
            })
            .catch(error => {
                console.error('Error fetching bookings:', error);
                bookingsList.innerHTML = '<div class="error-message"><i class="fas fa-exclamation-triangle"></i> Failed to load bookings. Please try again later.</div>';
            });
    }

    function renderBookings(bookingsToRender) {
        // Clear the list
        bookingsList.innerHTML = '';
        
        if (bookingsToRender.length === 0) {
            // Show empty state if no bookings
            bookingsList.style.display = 'none';
            emptyBookings.style.display = 'flex';
            return;
        }
        
        // Hide empty state and show list
        bookingsList.style.display = 'block';
        emptyBookings.style.display = 'none';
        
        // Create a container for the cards
        const bookingsCardList = document.createElement('div');
        bookingsCardList.className = 'booking-cards';
        
        // Render each booking
        bookingsToRender.forEach(booking => {
            const bookingCard = document.createElement('div');
            bookingCard.className = 'booking-card';
            
            // Add status class
            if (booking.status === 'CANCELED') {
                bookingCard.classList.add('canceled');
            } else if (new Date(booking.bookingDate) < new Date()) {
                bookingCard.classList.add('past');
            } else {
                bookingCard.classList.add('upcoming');
            }
            
            // Format date
            const bookingDateFormatted = new Date(booking.bookingDate).toLocaleDateString();
            
            // Default image
            const imagePath = booking.event.imagePath || 'images/default-event.jpg';
            
            bookingCard.innerHTML = `
                <div class="booking-card-image">
                    <img src="${imagePath}" alt="${booking.event.name}">
                    <div class="booking-status ${booking.status.toLowerCase()}">${formatStatus(booking.status)}</div>
                </div>
                <div class="booking-card-details">
                    <h3>${booking.event.name}</h3>
                    <p class="booking-date"><i class="fas fa-calendar"></i> ${bookingDateFormatted}</p>
                    <p class="booking-type">${formatEventType(booking.event.eventType)}</p>
                    <p class="booking-price">Rs. ${booking.event.price.toFixed(2)}</p>
                </div>
                <div class="booking-card-actions">
                    <button class="btn btn-primary btn-sm view-booking-btn" data-id="${booking.id}">
                        View Details
                    </button>
                    ${booking.status !== 'CANCELED' && new Date(booking.bookingDate) > new Date() ? 
                        `<button class="btn btn-danger btn-sm cancel-booking-btn" data-id="${booking.id}">
                            Cancel
                        </button>` : ''}
                </div>
            `;
            
            // Add event listeners to the buttons
            bookingCard.querySelector('.view-booking-btn').addEventListener('click', function() {
                const bookingId = this.getAttribute('data-id');
                const booking = bookings.find(b => b.id == bookingId);
                showBookingDetails(booking);
            });
            
            const cancelBtn = bookingCard.querySelector('.cancel-booking-btn');
            if (cancelBtn) {
                cancelBtn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    const bookingId = this.getAttribute('data-id');
                    const booking = bookings.find(b => b.id == bookingId);
                    openCancelModal(booking);
                });
            }
            
            bookingsCardList.appendChild(bookingCard);
        });
        
        bookingsList.appendChild(bookingsCardList);
    }

    function filterBookings() {
        const statusValue = bookingStatusFilter.value;
        const sortValue = bookingDateFilter.value;
        
        let filtered = [...bookings];
        
        // Filter by status
        if (statusValue !== 'all') {
            if (statusValue === 'upcoming') {
                filtered = filtered.filter(booking => 
                    booking.status !== 'CANCELED' && new Date(booking.bookingDate) > new Date()
                );
            } else if (statusValue === 'past') {
                filtered = filtered.filter(booking => 
                    booking.status !== 'CANCELED' && new Date(booking.bookingDate) < new Date()
                );
            } else if (statusValue === 'canceled') {
                filtered = filtered.filter(booking => booking.status === 'CANCELED');
            }
        }
        
        // Sort by date
        if (sortValue === 'newest') {
            filtered.sort((a, b) => new Date(b.bookingDate) - new Date(a.bookingDate));
        } else {
            filtered.sort((a, b) => new Date(a.bookingDate) - new Date(b.bookingDate));
        }
        
        renderBookings(filtered);
    }

    function showBookingDetails(booking) {
        currentBooking = booking;
        
        // Set booking details in the modal
        bookingEventName.textContent = booking.event.name;
        bookingId.textContent = booking.id;
        bookingDate.textContent = new Date(booking.bookingDate).toLocaleDateString();
        bookingStatus.textContent = formatStatus(booking.status);
        bookingPrice.textContent = `Rs. ${booking.event.price.toFixed(2)}`;
        bookingRequests.textContent = booking.specialRequests || 'None';
        
        // Set event details
        eventImage.src = booking.event.imagePath || 'images/default-event.jpg';
        eventType.textContent = formatEventType(booking.event.eventType);
        eventDuration.textContent = `${booking.event.durationHours} hours`;
        eventDescription.textContent = booking.event.description;
        
        // Apply status class to booking status
        bookingStatus.className = booking.status.toLowerCase();
        
        // Show or hide cancel button based on booking status and date
        if (booking.status === 'CANCELED' || new Date(booking.bookingDate) < new Date()) {
            cancelBookingBtn.style.display = 'none';
        } else {
            cancelBookingBtn.style.display = 'inline-block';
        }
        
        // Load vehicles for this event
        fetchEventVehicles(booking.event.id);
        
        // Show the modal
        bookingDetailsModal.style.display = 'block';
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
            vehicleCard.className = 'vehicle-card small';
            
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

    function openCancelModal(booking) {
        if (booking) {
            currentBooking = booking;
        }
        
        cancelEventName.textContent = currentBooking.event.name;
        confirmCancelModal.style.display = 'block';
    }

    function cancelBooking() {
        if (!currentBooking) return;
        
        // Show loading state
        confirmCancelBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
        confirmCancelBtn.disabled = true;
        cancelCancelBtn.disabled = true;
        
        // Send cancellation request to server
        fetch(`/api/event-bookings/${currentBooking.id}/cancel`, {
            method: 'PUT'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            // Close modal
            confirmCancelModal.style.display = 'none';
            
            // Update booking in list
            const index = bookings.findIndex(b => b.id === currentBooking.id);
            if (index !== -1) {
                bookings[index].status = 'CANCELED';
            }
            
            // Re-render bookings
            filterBookings();
            
            // Close details modal if open
            bookingDetailsModal.style.display = 'none';
            
            // Show success message
            showAlert('Success', 'Your booking has been canceled.', 'success');
        })
        .catch(error => {
            console.error('Error canceling booking:', error);
            showAlert('Error', 'Failed to cancel the booking. Please try again.', 'error');
        })
        .finally(() => {
            // Reset button state
            confirmCancelBtn.innerHTML = 'Yes, Cancel Booking';
            confirmCancelBtn.disabled = false;
            cancelCancelBtn.disabled = false;
        });
    }

    // Helper functions
    function formatEventType(type) {
        if (!type) return 'General';
        
        // Convert SNAKE_CASE to Title Case
        return type.replace(/_/g, ' ')
            .split(' ')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
            .join(' ');
    }

    function formatStatus(status) {
        if (!status) return 'Unknown';
        
        const statusMap = {
            'PENDING': 'Pending',
            'CONFIRMED': 'Confirmed',
            'CANCELED': 'Canceled',
            'COMPLETED': 'Completed'
        };
        
        return statusMap[status] || status;
    }

    function getVehicleTypeName(typeId) {
        const types = {
            1: 'Car',
            2: 'Van',
            3: 'Bike',
            4: 'Bus',
            5: 'Lorry'
        };
        return types[typeId] || 'Vehicle';
    }

    function showAlert(title, message, type) {
        // Check if alert container exists
        let alertContainer = document.querySelector('.alert-container');
        
        if (!alertContainer) {
            // Create alert container if it doesn't exist
            alertContainer = document.createElement('div');
            alertContainer.className = 'alert-container';
            document.body.appendChild(alertContainer);
        }
        
        // Create alert
        const alert = document.createElement('div');
        alert.className = `alert alert-${type}`;
        alert.innerHTML = `
            <div class="alert-title">${title}</div>
            <div class="alert-message">${message}</div>
            <button class="alert-close">&times;</button>
        `;
        
        // Add alert to container
        alertContainer.appendChild(alert);
        
        // Close alert when X is clicked
        alert.querySelector('.alert-close').addEventListener('click', function() {
            alert.remove();
        });
        
        // Auto close after 5 seconds
        setTimeout(() => {
            if (alert.parentNode) {
                alert.remove();
            }
        }, 5000);
    }
}); 