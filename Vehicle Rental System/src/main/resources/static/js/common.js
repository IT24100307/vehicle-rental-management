// Common JavaScript functionality shared across pages

// Check if user is logged in
function checkAuth() {
    const customer = JSON.parse(localStorage.getItem('customer'));
    
    // If not logged in and not on login or register page, redirect to login
    if (!customer && 
        !window.location.pathname.includes('login.html') && 
        !window.location.pathname.includes('register.html')) {
        window.location.href = 'login.html';
        return null;
    }
    
    return customer;
}

// Update the user name in the navigation
function updateUserInfo() {
    const customer = checkAuth();
    if (customer) {
        const userNameElement = document.getElementById('userName');
        if (userNameElement) {
            userNameElement.textContent = customer.name;
        }
    }
}

// Handle logout
function setupLogout() {
    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', function(e) {
            e.preventDefault();
            localStorage.removeItem('customer');
            window.location.href = 'login.html';
        });
    }
}

// Format currency (Rs.)
function formatCurrency(amount) {
    return `${amount.toFixed(2)} Rs.`;
}

// Format date
function formatDate(dateString) {
    const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
    return new Date(dateString).toLocaleDateString('en-US', options);
}

// Show error message
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

// Show success message
function showSuccessMessage(message, duration = 3000) {
    // Create success message element if it doesn't exist
    let successElement = document.getElementById('successMessage');
    if (!successElement) {
        successElement = document.createElement('div');
        successElement.id = 'successMessage';
        successElement.style.position = 'fixed';
        successElement.style.top = '20px';
        successElement.style.right = '20px';
        successElement.style.backgroundColor = '#4CAF50';
        successElement.style.color = 'white';
        successElement.style.padding = '15px';
        successElement.style.borderRadius = '5px';
        successElement.style.zIndex = '1000';
        successElement.style.boxShadow = '0 2px 10px rgba(0,0,0,0.2)';
        document.body.appendChild(successElement);
    }
    
    // Set message and show
    successElement.textContent = message;
    successElement.style.display = 'block';
    
    // Auto hide after specified duration
    setTimeout(() => {
        successElement.style.display = 'none';
    }, duration);
}

// Initialize modal functionality
function initModals() {
    // Get all modal elements
    const modals = document.querySelectorAll('.modal');
    
    // Get all close buttons
    const closeButtons = document.querySelectorAll('.close');
    
    // Add click event to close buttons
    closeButtons.forEach(button => {
        button.addEventListener('click', function() {
            modals.forEach(modal => {
                modal.style.display = 'none';
            });
        });
    });
    
    // Close modal when clicking outside
    window.addEventListener('click', function(event) {
        modals.forEach(modal => {
            if (event.target === modal) {
                modal.style.display = 'none';
            }
        });
    });
}

// Make API request with authentication
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
            // Try to parse error response as JSON, but handle cases where it's not JSON
            try {
                const errorData = await response.json();
                console.error('Error data:', errorData);
                throw new Error(errorData.message || 'An error occurred');
            } catch (jsonError) {
                // If JSON parsing failed, use status text as error message
                console.error('JSON parsing error:', jsonError);
                throw new Error(`Request failed: ${response.status} ${response.statusText}`);
            }
        }
        
        // Handle no-content responses (status 204)
        if (response.status === 204) {
            return null; // No content
        }
        
        // Check if there's actually content to parse
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            const text = await response.text();
            return text ? JSON.parse(text) : null;
        } else {
            return null;
        }
    } catch (error) {
        console.error('API Request Error:', error);
        throw error;
    }
}

// Initialize common elements when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    updateUserInfo();
    setupLogout();
    initModals();
});
