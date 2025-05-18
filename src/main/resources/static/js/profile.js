// Profile page functionality

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in
    const customer = checkAuth();
    if (!customer) return;
    
    // Get the profile form and skip button
    const profileForm = document.getElementById('profileForm');
    const skipButton = document.getElementById('skipButton');
    
    // Pre-fill any existing data
    if (customer.address) {
        document.getElementById('address').value = customer.address;
    }
    
    // Handle form submission
    profileForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        // Get form data
        const address = document.getElementById('address').value;
        
        try {
            // Show loading state
            document.querySelector('button[type="submit"]').textContent = 'Saving...';
            document.querySelector('button[type="submit"]').disabled = true;
            
            // Update the customer object
            const updatedCustomer = {
                ...customer,
                address: address
            };
            
            // Call the API to update customer profile
            const response = await apiRequest(`/customers/${customer.customerId}`, 'PUT', updatedCustomer);
            
            // Update localStorage
            localStorage.setItem('customer', JSON.stringify(response.customer));
            
            // Redirect to inventory page
            window.location.href = 'inventory.html';
            
        } catch (error) {
            // Reset button
            document.querySelector('button[type="submit"]').textContent = 'Save Information';
            document.querySelector('button[type="submit"]').disabled = false;
            
            // Show error message
            showError('error-message', error.message || 'Failed to update profile. Please try again.');
        }
    });
    
    // Handle skip button
    skipButton.addEventListener('click', function() {
        window.location.href = 'inventory.html';
    });
});

// Check if user is authenticated (copied from common.js for standalone functionality)
function checkAuth() {
    const customer = JSON.parse(localStorage.getItem('customer'));
    
    // If not logged in and not on login or register page, redirect to login
    if (!customer) {
        window.location.href = 'login.html';
        return null;
    }
    
    return customer;
}

// API request function (copied from common.js for standalone functionality)
async function apiRequest(endpoint, method = 'GET', data = null) {
    const baseUrl = '/api';
    const url = `${baseUrl}${endpoint}`;
    
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        }
    };
    
    if (data) {
        options.body = JSON.stringify(data);
    }
    
    try {
        const response = await fetch(url, options);
        
        // If response is not OK (status code outside 200-299)
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'An error occurred');
        }
        
        return await response.json();
    } catch (error) {
        console.error('API Request Error:', error);
        throw error;
    }
}

// Show error message function (copied from common.js for standalone functionality)
function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
        
        // Auto hide after 5 seconds
        setTimeout(() => {
            errorElement.style.display = 'none';
        }, 5000);
    }
}
