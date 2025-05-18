// Login page functionality

document.addEventListener('DOMContentLoaded', function() {
    // Check if already logged in
    const customer = JSON.parse(localStorage.getItem('customer'));
    if (customer) {
        // Redirect to inventory page if already logged in
        window.location.href = 'inventory.html';
        return;
    }
    
    // Get the login form
    const loginForm = document.getElementById('loginForm');
    
    // Handle form submission
    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        // Get form data
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        
        try {
            // Show loading state
            document.querySelector('button[type="submit"]').textContent = 'Logging in...';
            document.querySelector('button[type="submit"]').disabled = true;
              // Call the API
            const response = await apiRequest('/customers/login', 'POST', {
                email: email,
                password: password
            });
            
            // Log the response to see what customer data we're getting
            console.log('Login API response:', response);
            
            // Ensure customer data is complete
            if (response.customer && response.customer.customerId) {
                // Store customer data in localStorage
                localStorage.setItem('customer', JSON.stringify(response.customer));
                
                // Redirect to profile page to complete personal information
                // or to inventory page if profile is already completed
                if (!response.customer.address) {
                    window.location.href = 'profile.html';
                } else {
                    window.location.href = 'inventory.html';
                }
            } else {
                throw new Error('Invalid customer data received');
            }
            
        } catch (error) {
            // Reset button
            document.querySelector('button[type="submit"]').textContent = 'Login';
            document.querySelector('button[type="submit"]').disabled = false;
            
            // Show error message
            showError('error-message', error.message || 'Invalid email or password');
        }
    });
});

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
