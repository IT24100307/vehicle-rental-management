// Register page functionality

document.addEventListener('DOMContentLoaded', function() {
    // Check if already logged in
    const customer = JSON.parse(localStorage.getItem('customer'));
    if (customer) {
        // Redirect to inventory page if already logged in
        window.location.href = 'inventory.html';
        return;
    }
    
    // Get the register form
    const registerForm = document.getElementById('registerForm');
    
    // Handle form submission
    registerForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        // Get form data
        const name = document.getElementById('name').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const contactNumber = document.getElementById('contactNumber').value;
        const driverLicenseNumber = document.getElementById('driverLicenseNumber').value;
        
        try {
            // Show loading state
            document.querySelector('button[type="submit"]').textContent = 'Registering...';
            document.querySelector('button[type="submit"]').disabled = true;
            
            // Call the API
            const response = await apiRequest('/customers/register', 'POST', {
                name: name,
                email: email,
                password: password,
                contactNumber: contactNumber,
                driverLicenseNumber: parseInt(driverLicenseNumber)
            });
            
            // Store customer data in localStorage
            localStorage.setItem('customer', JSON.stringify(response.customer));
            
            // Redirect to profile page to complete personal information
            window.location.href = 'profile.html';
            
        } catch (error) {
            // Reset button
            document.querySelector('button[type="submit"]').textContent = 'Register';
            document.querySelector('button[type="submit"]').disabled = false;
            
            // Show error message
            showError('error-message', error.message || 'Registration failed. Please try again.');
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
