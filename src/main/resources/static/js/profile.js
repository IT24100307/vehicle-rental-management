// Profile page functionality

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in
    const customer = checkAuth();
    if (!customer) return;
    
    // Initialize tab functionality
    initTabs();
    
    // Display customer information
    displayCustomerInfo(customer);
    
    // Pre-fill the update form with current details
    populateUpdateForm(customer);
    
    // Setup event listeners
    setupEventListeners(customer);
});

// Initialize tab functionality
function initTabs() {
    const tabs = document.querySelectorAll('.tab');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            // Remove active class from all tabs
            tabs.forEach(t => t.classList.remove('active'));
            // Add active class to clicked tab
            this.classList.add('active');
            
            // Hide all tab contents
            tabContents.forEach(content => content.classList.remove('active'));
            
            // Show the selected tab content
            const tabId = this.getAttribute('data-tab');
            document.getElementById(`${tabId}-tab`).classList.add('active');
        });
    });
}

// Display customer information in view tab
function displayCustomerInfo(customer) {
    document.getElementById('customer-id').textContent = customer.customerId;
    document.getElementById('customer-name').textContent = customer.name || 'Not provided';
    document.getElementById('customer-email').textContent = customer.email || 'Not provided';
    document.getElementById('customer-contact').textContent = customer.contactNumber || 'Not provided';
    document.getElementById('customer-license').textContent = customer.driverLicenseNumber || 'Not provided';
    document.getElementById('customer-address').textContent = customer.address || 'Not provided';
}

// Pre-fill the update form with customer details
function populateUpdateForm(customer) {
    document.getElementById('name').value = customer.name || '';
    document.getElementById('contactNumber').value = customer.contactNumber || '';
    document.getElementById('driverLicenseNumber').value = customer.driverLicenseNumber || '';
    document.getElementById('address').value = customer.address || '';
}

// Setup all event listeners
function setupEventListeners(customer) {
    // Get form and button elements
    const profileForm = document.getElementById('profileForm');
    const deleteAccountForm = document.getElementById('deleteAccountForm');
    const goToUpdateBtn = document.getElementById('goToUpdateBtn');
    const cancelUpdateBtn = document.getElementById('cancelUpdateBtn');
    const backToInventoryBtn = document.getElementById('backToInventoryBtn');
    
    // Update profile form submission
    profileForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        await updateProfile(customer);
    });
    
    // Delete account form submission
    deleteAccountForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        await deleteAccount(customer);
    });
    
    // Go to update tab button
    goToUpdateBtn.addEventListener('click', function() {
        document.querySelector('.tab[data-tab="update"]').click();
    });
    
    // Cancel update button
    cancelUpdateBtn.addEventListener('click', function() {
        // Reset form with original customer data
        populateUpdateForm(customer);
        // Go back to view tab
        document.querySelector('.tab[data-tab="view"]').click();
    });
    
    // Back to inventory button
    backToInventoryBtn.addEventListener('click', function() {
        window.location.href = 'inventory.html';
    });
}

// Update customer profile
async function updateProfile(customer) {
    // Get form data
    const name = document.getElementById('name').value;
    const contactNumber = document.getElementById('contactNumber').value;
    const driverLicenseNumber = document.getElementById('driverLicenseNumber').value;
    const address = document.getElementById('address').value;
    
    // Show loading state
    const submitBtn = document.querySelector('#profileForm button[type="submit"]');
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
    submitBtn.disabled = true;
    
    try {
        // Create updated customer object
        const updatedCustomer = {
            ...customer,
            name: name,
            contactNumber: contactNumber,
            driverLicenseNumber: driverLicenseNumber,
            address: address
        };
        
        // Call the API to update customer profile
        const response = await apiRequest(`/customers/${customer.customerId}`, 'PUT', updatedCustomer);
        
        // Update localStorage with new customer data
        localStorage.setItem('customer', JSON.stringify(response.customer));
        
        // Show success message
        const successMessage = document.getElementById('success-message');
        successMessage.textContent = 'Profile updated successfully!';
        successMessage.style.display = 'block';
        
        // Hide success message after 3 seconds
        setTimeout(() => {
            successMessage.style.display = 'none';
        }, 3000);
        
        // Update the displayed customer info
        displayCustomerInfo(response.customer);
        
        // Switch to view tab
        document.querySelector('.tab[data-tab="view"]').click();
        
    } catch (error) {
        // Show error message
        showError('error-message', error.message || 'Failed to update profile. Please try again.');
    } finally {
        // Reset button state
        submitBtn.innerHTML = '<i class="fas fa-save"></i> Save Changes';
        submitBtn.disabled = false;
    }
}

// Delete customer account
async function deleteAccount(customer) {
    // Get password
    const password = document.getElementById('password').value;
    
    // Validate password is provided
    if (!password) {
        showError('error-message', 'Please enter your password to confirm account deletion.');
        return;
    }
    
    // Confirm deletion with user
    if (!confirm('Are you sure you want to permanently delete your account? This action cannot be undone.')) {
        return;
    }
    
    // Show loading state
    const deleteBtn = document.querySelector('#deleteAccountForm button[type="submit"]');
    deleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
    deleteBtn.disabled = true;
    
    try {
        // First verify password
        await apiRequest('/customers/login', 'POST', {
            email: customer.email,
            password: password
        });
        
        // If password verification passed, delete the account
        await apiRequest(`/customers/${customer.customerId}`, 'DELETE');
        
        // Remove customer from localStorage
        localStorage.removeItem('customer');
        
        // Show success message and redirect to login page
        alert('Your account has been deleted successfully.');
        window.location.href = 'login.html';
        
    } catch (error) {
        // Show error message
        showError('error-message', error.message || 'Failed to delete account. Please check your password and try again.');
        
        // Reset button state
        deleteBtn.innerHTML = '<i class="fas fa-trash"></i> Permanently Delete My Account';
        deleteBtn.disabled = false;
    }
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
