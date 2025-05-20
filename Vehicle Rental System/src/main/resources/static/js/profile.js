// Profile page functionality

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in
    const customer = checkAuth();
    if (!customer) return;
    
    // Get form elements
    const profileForm = document.getElementById('profileForm');
    const cancelButton = document.getElementById('cancelButton');
    const deleteAccountBtn = document.getElementById('deleteAccountBtn');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
    const deleteAccountModal = document.getElementById('deleteAccountModal');
    const closeModalSpan = document.querySelector('#deleteAccountModal .close');
    const successMessage = document.getElementById('success-message');
    
    // Pre-fill form with customer data
    fillFormWithCustomerData(customer);
    
    // Handle form submission
    profileForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        // Clear any previous error messages
        showError('error-message', '');
        
        // Get form data
        const name = document.getElementById('name').value.trim();
        const contactNumber = document.getElementById('contactNumber').value.trim();
        const address = document.getElementById('address').value.trim();
        const driverLicenseNumber = document.getElementById('driverLicenseNumber').value.trim();
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        
        // Basic validation
        if (!name) {
            return showError('error-message', 'Please enter your name');
        }
        
        if (!contactNumber) {
            return showError('error-message', 'Please enter your contact number');
        }
        
        // Check if password fields are filled
        if (currentPassword || newPassword || confirmPassword) {
            // All password fields must be filled if any are filled
            if (!currentPassword || !newPassword || !confirmPassword) {
                return showError('error-message', 'Please fill all password fields to change password');
            }
            
            // New password must match confirmation
            if (newPassword !== confirmPassword) {
                return showError('error-message', 'New password and confirmation do not match');
            }
        }
        
        try {
            // Show loading state
            const submitButton = document.querySelector('button[type="submit"]');
            const originalButtonText = submitButton.innerHTML;
            submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
            submitButton.disabled = true;
            
            // Create updated customer object
            const updatedCustomer = {
                ...customer,
                name,
                contactNumber,
                address,
                driverLicenseNumber
            };
            
            // Add password properties if they exist
            if (currentPassword) {
                updatedCustomer.currentPassword = currentPassword;
                updatedCustomer.newPassword = newPassword;
            }
            
            // Call the API to update customer profile
            const response = await apiRequest(`/customers/${customer.customerId}`, 'PUT', updatedCustomer);
            
            // Update localStorage
            localStorage.setItem('customer', JSON.stringify(response.customer));
            
            // Reset password fields
            document.getElementById('currentPassword').value = '';
            document.getElementById('newPassword').value = '';
            document.getElementById('confirmPassword').value = '';
            
            // Show success message
            showSuccess('Your profile has been updated successfully.');
            
            // Reset button
            submitButton.innerHTML = originalButtonText;
            submitButton.disabled = false;
            
        } catch (error) {
            // Reset button
            document.querySelector('button[type="submit"]').innerHTML = '<i class="fas fa-save"></i> Save Changes';
            document.querySelector('button[type="submit"]').disabled = false;
            
            // Show error message
            showError('error-message', error.message || 'Failed to update profile. Please try again.');
        }
    });
    
    // Handle cancel button
    cancelButton.addEventListener('click', function() {
        window.location.href = 'inventory.html';
    });
    
    // Delete account functionality
    deleteAccountBtn.addEventListener('click', function() {
        // Show the delete account confirmation modal
        deleteAccountModal.style.display = 'block';
    });
    
    // Close the modal when the user clicks the x
    closeModalSpan.addEventListener('click', function() {
        deleteAccountModal.style.display = 'none';
    });
    
    // Close the modal when the user clicks Cancel
    cancelDeleteBtn.addEventListener('click', function() {
        deleteAccountModal.style.display = 'none';
        document.getElementById('deleteConfirmPassword').value = '';
        document.getElementById('deleteErrorMessage').textContent = '';
    });
    
    // Handle the delete confirmation button
    confirmDeleteBtn.addEventListener('click', async function() {
        // Clear any previous error messages
        document.getElementById('deleteErrorMessage').textContent = '';
        
        const password = document.getElementById('deleteConfirmPassword').value;
        if (!password) {
            document.getElementById('deleteErrorMessage').textContent = 'Please enter your password to confirm deletion';
            return;
        }
        
        try {
            // Show loading state
            confirmDeleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
            confirmDeleteBtn.disabled = true;
            cancelDeleteBtn.disabled = true;
            
            // API call to delete the account
            await apiRequest(`/customers/${customer.customerId}`, 'DELETE', { password });
            
            // Clear the user data from local storage
            localStorage.removeItem('customer');
            
            // Show success message
            alert('Your account has been deleted successfully.');
            
            // Redirect to the homepage or login page
            window.location.href = 'login.html';
            
        } catch (error) {
            // Reset button
            confirmDeleteBtn.innerHTML = 'Delete My Account';
            confirmDeleteBtn.disabled = false;
            cancelDeleteBtn.disabled = false;
            
            // Show error message
            document.getElementById('deleteErrorMessage').textContent = 
                error.message || 'Failed to delete account. Please check your password and try again.';
        }
    });
    
    // Close the modal when clicking anywhere outside of it
    window.addEventListener('click', function(event) {
        if (event.target == deleteAccountModal) {
            deleteAccountModal.style.display = 'none';
        }
    });
});

// Function to fill the form with customer data
function fillFormWithCustomerData(customer) {
    document.getElementById('name').value = customer.name || '';
    document.getElementById('email').value = customer.email || '';
    document.getElementById('contactNumber').value = customer.contactNumber || '';
    document.getElementById('address').value = customer.address || '';
    document.getElementById('driverLicenseNumber').value = customer.driverLicenseNumber || '';
}

// Function to show success message
function showSuccess(message) {
    const successElement = document.getElementById('success-message');
    if (successElement) {
        successElement.textContent = message;
        successElement.style.display = 'block';
        
        // Auto hide after 5 seconds
        setTimeout(() => {
            successElement.style.display = 'none';
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

// Show error message function (copied from common.js for standalone functionality)
function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = message ? 'block' : 'none';
        
        // Auto hide after 5 seconds if there is a message
        if (message) {
            setTimeout(() => {
                errorElement.style.display = 'none';
            }, 5000);
        }
    }
}
