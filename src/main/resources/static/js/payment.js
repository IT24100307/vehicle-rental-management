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
    
    if (!purchaseId) {
        alert('No rental ID provided. Redirecting to rentals page.');
        window.location.href = 'rentals.html';
        return;
    }
    
    // Initialize payment methods
    initPaymentMethods();
    
    // Load rental details
    loadRentalDetails(purchaseId);
    
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
                </div>
                <div class="rental-details-row total-cost">
                    <div><strong>Total Cost:</strong> ${formatCurrency(totalCost)}</div>
                </div>
            </div>
        `;
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
            showError('error-message', 'No rental found. Please try again.');
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
            
            // Process payment
            const response = await apiRequest(`/payments/process/${currentRental.purchaseId}`, 'POST', payment);
            
            // Show confirmation modal
            showPaymentConfirmation(response.payment);
            
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
            showError('error-message', 'No rental found. Please try again.');
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
            
            // Process payment
            const response = await apiRequest(`/payments/process/${currentRental.purchaseId}`, 'POST', payment);
            
            // Show confirmation modal
            showPaymentConfirmation(response.payment);
            
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
        window.location.href = 'rentals.html';
    });
}

// Show payment confirmation modal
function showPaymentConfirmation(payment) {
    const modal = document.getElementById('paymentConfirmationModal');
    const paymentDetails = document.getElementById('paymentDetails');
    
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
}
