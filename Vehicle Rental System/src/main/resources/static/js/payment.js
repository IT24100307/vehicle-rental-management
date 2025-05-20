// Payment page functionality

// Global variables
let currentRental = null;
let selectedPaymentMethod = null;

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in
    const customer = checkAuth();
    if (!customer) return;
    
    // Get purchase ID from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const purchaseId = urlParams.get('purchaseId');
    const eventBookingId = urlParams.get('eventBookingId');
    
    if (!purchaseId && !eventBookingId) {
        alert('No payment ID provided. Redirecting to home page.');
        window.location.href = 'inventory.html';
        return;
    }
    
    // Initialize payment methods
    initPaymentMethods();
    
    // Load details based on what type of payment this is
    if (purchaseId) {
        // This is a vehicle rental payment
        loadRentalDetails(purchaseId);
    } else if (eventBookingId) {
        // This is an event booking payment
        loadEventBookingDetails(eventBookingId);
    }
    
    // Init card form
    initCardForm();
    
    // Init cash payment
    initCashPayment();
    
    // Init confirmation modal
    initConfirmationModal();
});

// Load rental details
async function loadRentalDetails(purchaseId) {
    try {
        const rentalDetails = document.getElementById('vehicleDetails');
        rentalDetails.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading rental details...</div>';
        
        // Call the API to get rental details
        const rental = await apiRequest(`/customers/rental/${purchaseId}`);
        
        if (!rental) {
            rentalDetails.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Rental not found. Please try again or contact support.</p>
                </div>
            `;
            return;
        }
        
        // Store rental globally
        currentRental = rental;
        
        // Display rental details
        const vehicle = rental.vehicle;
        const totalCost = vehicle.rentPrice * rental.rentalDays;
        
        rentalDetails.innerHTML = `
            <div class="vehicle-info">
                <h3>${vehicle.brand} ${vehicle.model}</h3>
                <div class="rental-details-row">
                    <div><strong>Rental ID:</strong> ${rental.purchaseId}</div>
                    <div><strong>Rental Date:</strong> ${formatDate(rental.purchaseDate)}</div>
                </div>
                <div class="rental-details-row">
                    <div><strong>Duration:</strong> ${rental.rentalDays} days</div>
                    <div><strong>Daily Rate:</strong> ${formatCurrency(vehicle.rentPrice)}</div>
                </div>            <div class="rental-details-row total-cost">
                    <div><strong>Total Cost:</strong> ${formatCurrency(totalCost)}</div>
                </div>
            </div>
        `;
        
        // Update payment amount
        document.getElementById('paymentAmount').textContent = formatCurrency(totalCost);
    } catch (error) {
        console.error('Error loading rental details:', error);
        document.getElementById('vehicleDetails').innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                <p>Failed to load rental details: ${error.message}</p>
            </div>
        `;
    }
}

// Load event booking details
async function loadEventBookingDetails(eventBookingId) {
    try {
        const detailsContainer = document.getElementById('vehicleDetails');
        detailsContainer.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading event details...</div>';
        
        // Call the API to get event booking details
        const response = await fetch(`/api/event-bookings/${eventBookingId}`);
        
        if (!response.ok) {
            throw new Error(`Failed to load event booking: ${response.status}`);
        }
        
        const eventBooking = await response.json();
        
        if (!eventBooking) {
            detailsContainer.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Event booking not found. Please try again or contact support.</p>
                </div>
            `;
            return;
        }
        
        // Store booking globally
        currentRental = eventBooking; // Reuse the currentRental variable for consistency
        
        // Get the event details
        const event = eventBooking.event;
        
        if (!event) {
            detailsContainer.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Event information is missing. Please try again or contact support.</p>
                </div>
            `;
            return;
        }
        
        // Update the section title to reflect we're paying for an event
        const sectionHeader = document.querySelector('.section-header h1');
        if (sectionHeader) {
            sectionHeader.innerHTML = '<i class="fas fa-credit-card"></i> Event Payment';
        }
        
        // Display event details
        detailsContainer.innerHTML = `
            <div class="vehicle-info">
                <h3>${event.name}</h3>
                <div class="rental-details-row">
                    <div><strong>Booking ID:</strong> ${eventBooking.id}</div>
                    <div><strong>Event Type:</strong> ${formatEventType(event.eventType)}</div>
                </div>
                <div class="rental-details-row">
                    <div><strong>Booking Date:</strong> ${formatDate(eventBooking.bookingDate)}</div>
                    <div><strong>Duration:</strong> ${event.durationHours} hours</div>
                </div>
                <div class="rental-details-row total-cost">
                    <div><strong>Total Cost:</strong> ${formatCurrency(event.price)}</div>
                </div>
            </div>
        `;
    } catch (error) {
        console.error('Error loading event booking details:', error);
        document.getElementById('vehicleDetails').innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                <p>Failed to load event details: ${error.message}</p>
            </div>
        `;
    }
}

// Initialize payment methods
function initPaymentMethods() {
    const cardMethod = document.getElementById('cardMethod');
    const cashMethod = document.getElementById('cashMethod');
    const cardForm = document.getElementById('cardPaymentForm');
    const cashForm = document.getElementById('cashPaymentForm');
    
    cardMethod.addEventListener('click', function() {
        cardMethod.classList.add('selected');
        cashMethod.classList.remove('selected');
        cardForm.style.display = 'block';
        cashForm.style.display = 'none';
        selectedPaymentMethod = 'CARD';
    });
    
    cashMethod.addEventListener('click', function() {
        cashMethod.classList.add('selected');
        cardMethod.classList.remove('selected');
        cashForm.style.display = 'block';
        cardForm.style.display = 'none';
        selectedPaymentMethod = 'CASH';
    });
}

// Initialize card payment form
function initCardForm() {
    const form = document.getElementById('creditCardForm');
    const backBtn = document.getElementById('backBtn');
    
    // Format card expiry as MM/YY
    const expiryInput = document.getElementById('cardExpiry');
    expiryInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');
        if (value.length > 2) {
            value = value.substring(0, 2) + '/' + value.substring(2, 4);
        }
        e.target.value = value;
    });
    
    // Only allow numbers in card number field
    const cardNumberInput = document.getElementById('cardNumber');
    cardNumberInput.addEventListener('input', function(e) {
        e.target.value = e.target.value.replace(/\D/g, '');
    });
    
    // Only allow numbers in CVC field
    const cvcInput = document.getElementById('cardCvc');
    cvcInput.addEventListener('input', function(e) {
        e.target.value = e.target.value.replace(/\D/g, '');
    });
      // Back button returns to payment method selection
    backBtn.addEventListener('click', function() {
        document.getElementById('cardPaymentForm').style.display = 'none';
        document.getElementById('cardMethod').classList.remove('selected');
        selectedPaymentMethod = null;
    });
    
    // Handle form submission
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        if (!currentRental) {
            showError('error-message', 'No payment information found. Please try again.');
            return;
        }
        
        const cardHolderName = document.getElementById('cardHolderName').value;
        const cardNumber = document.getElementById('cardNumber').value;
        const cardExpiry = document.getElementById('cardExpiry').value;
        const cardCvc = document.getElementById('cardCvc').value;
        const cardType = document.getElementById('cardType').value;
        
        // Validation
        if (cardNumber.length < 16) {
            showError('error-message', 'Please enter a valid 16-digit card number.');
            return;
        }
        
        if (cardExpiry.length !== 5 || !cardExpiry.includes('/')) {
            showError('error-message', 'Please enter a valid expiry date (MM/YY).');
            return;
        }
        
        if (cardCvc.length !== 3) {
            showError('error-message', 'Please enter a valid 3-digit CVC code.');
            return;
        }
        
        if (!cardType) {
            showError('error-message', 'Please select a card type.');
            return;
        }
        
        try {
            // Show loading state
            const submitBtn = form.querySelector('button[type="submit"]');
            submitBtn.textContent = 'Processing...';
            submitBtn.disabled = true;
            
            // Create payment object
            const payment = {
                paymentMethod: 'CARD',
                cardHolderName: cardHolderName,
                cardNumber: cardNumber,
                cardExpiry: cardExpiry,
                cardType: cardType
            };
            
            // Determine if this is a vehicle rental or event booking
            let response;
            const urlParams = new URLSearchParams(window.location.search);
            const isEventBooking = urlParams.has('eventBookingId');
            
            if (isEventBooking) {
                // Process event booking payment
                const bookingId = urlParams.get('eventBookingId');
                const url = `/api/payments/process-event/${bookingId}`;
                
                console.log('Processing event payment:', url);
                response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(payment)
                });
            } else {
                // Process vehicle rental payment
                const purchaseId = urlParams.get('purchaseId');
                const url = `/api/payments/process/${purchaseId}`;
                
                console.log('Processing rental payment:', url);
                response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(payment)
                });
            }
            
            if (!response.ok) {
                throw new Error(`Payment failed: ${response.status}`);
            }
            
            const responseData = await response.json();
            console.log('Payment response:', responseData);
            
            // Show confirmation modal
            showPaymentConfirmation(responseData.payment);
            
            // Reset form
            form.reset();
            
        } catch (error) {
            console.error('Error processing payment:', error);
            showError('error-message', 'Failed to process payment: ' + error.message);
        } finally {
            // Reset button
            const submitBtn = form.querySelector('button[type="submit"]');
            submitBtn.textContent = 'Confirm Payment';
            submitBtn.disabled = false;
        }
    });
}

// Initialize cash payment
function initCashPayment() {
    const backBtn = document.getElementById('cashBackBtn');
    const confirmBtn = document.getElementById('confirmCashBtn');
    
    // Back button returns to payment method selection
    backBtn.addEventListener('click', function() {
        document.getElementById('cashPaymentForm').style.display = 'none';
        document.getElementById('cashMethod').classList.remove('selected');
        selectedPaymentMethod = null;
    });
      // Confirm cash payment
    confirmBtn.addEventListener('click', async function() {
        if (!currentRental) {
            showError('error-message', 'No payment information found. Please try again.');
            return;
        }
        
        try {
            // Show loading state
            confirmBtn.textContent = 'Processing...';
            confirmBtn.disabled = true;
            
            // Create payment object
            const payment = {
                paymentMethod: 'CASH'
            };
            
            // Determine if this is a vehicle rental or event booking
            let response;
            const urlParams = new URLSearchParams(window.location.search);
            const isEventBooking = urlParams.has('eventBookingId');
            
            if (isEventBooking) {
                // Process event booking payment
                const bookingId = urlParams.get('eventBookingId');
                const url = `/api/payments/process-event/${bookingId}`;
                
                console.log('Processing event cash payment:', url);
                response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(payment)
                });
            } else {
                // Process vehicle rental payment
                const purchaseId = urlParams.get('purchaseId');
                const url = `/api/payments/process/${purchaseId}`;
                
                console.log('Processing rental cash payment:', url);
                response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(payment)
                });
            }
            
            if (!response.ok) {
                throw new Error(`Payment failed: ${response.status}`);
            }
            
            const responseData = await response.json();
            console.log('Payment response:', responseData);
            
            // Show confirmation modal
            showPaymentConfirmation(responseData.payment);
            
        } catch (error) {
            console.error('Error processing payment:', error);
            showError('error-message', 'Failed to process payment: ' + error.message);
        } finally {
            // Reset button
            confirmBtn.textContent = 'Confirm';
            confirmBtn.disabled = false;
        }
    });
}

// Initialize confirmation modal
function initConfirmationModal() {
    const viewRentalsBtn = document.getElementById('viewRentalsBtn');
    
    viewRentalsBtn.addEventListener('click', function() {
        // Determine if we should go to rentals or events page based on payment type
        const urlParams = new URLSearchParams(window.location.search);
        const isEventBooking = urlParams.has('eventBookingId');
        
        if (isEventBooking) {
            window.location.href = 'event-bookings.html';
        } else {
            window.location.href = 'rentals.html';
        }
    });
}

// Show payment confirmation modal
function showPaymentConfirmation(payment) {
    const modal = document.getElementById('paymentConfirmationModal');
    const paymentDetails = document.getElementById('paymentDetails');
    const viewRentalsBtn = document.getElementById('viewRentalsBtn');
      // Determine if this is an event booking
    const urlParams = new URLSearchParams(window.location.search);
    const isEventBooking = urlParams.has('eventBookingId');
    
    // Update button text based on payment type
    if (isEventBooking) {
        viewRentalsBtn.textContent = 'View My Event Bookings';
        
        // Set automatic redirect after 3 seconds
        setTimeout(() => {
            window.location.href = 'event-bookings.html';
        }, 3000);
    } else {
        viewRentalsBtn.textContent = 'View My Rentals';
    }
    
    // Build payment details HTML
    let detailsHtml = `
        <div class="payment-confirmation-details">
            <div><strong>Payment ID:</strong> ${payment.paymentId}</div>
            <div><strong>Amount:</strong> ${formatCurrency(payment.amount)}</div>
            <div><strong>Payment Method:</strong> ${payment.paymentMethod}</div>
            <div><strong>Date:</strong> ${formatDate(payment.paymentDate)}</div>
            <div><strong>Status:</strong> ${payment.status}</div>
    `;
    
    // Add card-specific details if it's a card payment
    if (payment.paymentMethod === 'CARD') {
        detailsHtml += `
            <div><strong>Card Type:</strong> ${payment.cardType}</div>
            <div><strong>Card Number:</strong> ${payment.cardNumber}</div>
        `;
    }
    
    detailsHtml += `</div>`;
    
    paymentDetails.innerHTML = detailsHtml;
      // Show modal
    modal.style.display = 'block';
    
    // Add a redirect message if this is an event booking
    if (isEventBooking) {
        const redirectMsg = document.createElement('div');
        redirectMsg.className = 'redirect-message';
        redirectMsg.innerHTML = '<p>Redirecting to My Event Bookings in 3 seconds...</p>';
        redirectMsg.style.textAlign = 'center';
        redirectMsg.style.marginTop = '15px';
        redirectMsg.style.color = '#3498db';
        paymentDetails.appendChild(redirectMsg);
    }
}

// Helper function to format event type
function formatEventType(type) {
    if (!type) return 'General';
    
    // Convert SNAKE_CASE to Title Case
    return type.replace(/_/g, ' ')
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
        .join(' ');
}
