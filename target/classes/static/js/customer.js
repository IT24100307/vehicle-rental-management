/**
 * Customer Management JavaScript
 * This file contains functions for handling CRUD operations from the frontend
 */

// DOM Elements and Variables
let customers = [];
let customerModal;
let viewModal;

document.addEventListener('DOMContentLoaded', function() {
    // Initialize Bootstrap modals
    customerModal = new bootstrap.Modal(document.getElementById('customerModal'));
    viewModal = new bootstrap.Modal(document.getElementById('viewCustomerModal'));
    
    // Initialize the customer table with data
    loadCustomers();
    
    // Add event listeners for CRUD operations
    const customerForm = document.getElementById('customerForm');
    if (customerForm) {
        customerForm.addEventListener('submit', handleFormSubmit);
    }
    
    // Add event listeners for search and filter
    const searchButton = document.getElementById('searchButton');
    if (searchButton) {
        searchButton.addEventListener('click', handleSearch);
    }
    
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keyup', function(event) {
            if (event.key === 'Enter') {
                handleSearch();
            }
        });
    }
    
    const filterSelect = document.getElementById('filterSelect');
    if (filterSelect) {
        filterSelect.addEventListener('change', handleFilter);
    }
    
    // Setup modal events
    const customerModalElement = document.getElementById('customerModal');
    if (customerModalElement) {
        customerModalElement.addEventListener('show.bs.modal', function(event) {
            const button = event.relatedTarget;
            const isEdit = button && button.getAttribute('data-action') === 'edit';
            
            document.getElementById('customerModalLabel').textContent = 
                isEdit ? 'Edit Customer' : 'Add New Customer';
            
            if (!isEdit) {
                resetForm();
            }
        });
    }
});

// Load all customers and display in table
function loadCustomers() {
    fetch('/api/customers')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch customers');
            }
            return response.json();
        })
        .then(data => {
            customers = data;
            displayCustomers(customers);
            updateDashboard(customers);
        })
        .catch(error => {
            console.error('Error loading customers:', error);
            showAlert('Error loading customers. Please try again.', 'danger');
        });
}

// Display customers in the table
function displayCustomers(customers) {
    const tableBody = document.getElementById('customerTableBody');
    if (!tableBody) return;
    
    tableBody.innerHTML = '';
    
    if (customers.length === 0) {
        document.getElementById('noCustomersMessage').style.display = 'block';
        document.getElementById('customerTable').style.display = 'none';
        return;
    }
    
    document.getElementById('noCustomersMessage').style.display = 'none';
    document.getElementById('customerTable').style.display = 'table';
    
    customers.forEach(customer => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${customer.cusID}</td>
            <td>${customer.cusName}</td>
            <td>${customer.contactNum}</td>
            <td>${customer.rentedVehicle}</td>
            <td>
                <button class="btn btn-sm btn-info btn-action" onclick="viewCustomerDetails('${customer.cusID}')">
                    <i class="bi bi-eye"></i> View
                </button>
                <button class="btn btn-sm btn-warning btn-action" onclick="loadCustomerForEdit('${customer.cusID}')">
                    <i class="bi bi-pencil"></i> Edit
                </button>
                <button class="btn btn-sm btn-danger btn-action" onclick="deleteCustomer('${customer.cusID}')">
                    <i class="bi bi-trash"></i> Delete
                </button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

// Update dashboard metrics
function updateDashboard(customers) {
    const customerCount = document.getElementById('customerCount');
    const rentedCount = document.getElementById('rentedCount');
    const activeCount = document.getElementById('activeCount');
    
    if (customerCount) {
        customerCount.textContent = customers.length;
    }
    
    if (rentedCount) {
        // Count unique rented vehicles
        const rentedVehicles = new Set(customers.map(c => c.rentedVehicle).filter(v => v > 0));
        rentedCount.textContent = rentedVehicles.size;
    }
    
    if (activeCount) {
        // Count customers with active rentals (not returned)
        const activeRentals = customers.filter(c => c.rentedVehicle > 0 && !c.returnrented).length;
        activeCount.textContent = activeRentals;
    }
}

// Handle form submission (create or update)
function handleFormSubmit(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const customer = Object.fromEntries(formData.entries());
    
    // Convert numeric fields from string to number
    customer.contactNum = parseInt(customer.contactNum);
    customer.driverLicenseNumber = parseInt(customer.driverLicenseNumber);
    customer.rentedVehicle = parseInt(customer.rentedVehicle);
    customer.nodays = parseInt(customer.nodays || 1);
    
    // Convert checkbox to boolean
    customer.returnrented = !!customer.returnrented;
    
    const isUpdating = !!customer.cusID;
    const url = isUpdating ? `/api/customers/${customer.cusID}` : '/api/customers';
    const method = isUpdating ? 'PUT' : 'POST';
    
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(customer)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to save customer');
        }
        return response.json();
    })
    .then(savedCustomer => {
        showAlert(`Customer ${isUpdating ? 'updated' : 'created'} successfully!`, 'success');
        customerModal.hide();
        loadCustomers();
    })
    .catch(error => {
        console.error('Error saving customer:', error);
        showAlert('Error saving customer. Please try again.', 'danger');
    });
}

// Load customer data for viewing details
function viewCustomerDetails(id) {
    fetch(`/api/customers/${id}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch customer details');
            }
            return response.json();
        })
        .then(customer => {
            const detailsDiv = document.getElementById('viewCustomerDetails');
            detailsDiv.innerHTML = `
                <div class="customer-details">
                    <p><strong>ID:</strong> ${customer.cusID}</p>
                    <p><strong>Name:</strong> ${customer.cusName}</p>
                    <p><strong>Contact Number:</strong> ${customer.contactNum}</p>
                    <p><strong>Driver License Number:</strong> ${customer.driverLicenseNumber}</p>
                    <p><strong>Rented Vehicle ID:</strong> ${customer.rentedVehicle}</p>
                    <p><strong>Rental Status:</strong> 
                        <span class="badge ${customer.returnrented ? 'bg-success' : 'bg-warning'}">
                            ${customer.returnrented ? 'Returned' : 'Active Rental'}
                        </span>
                    </p>
                    <p><strong>Rental Days:</strong> ${customer.nodays || 'N/A'}</p>
                </div>
                <div class="mt-3">
                    <button class="btn btn-warning" onclick="loadCustomerForEdit('${customer.cusID}'); viewModal.hide();">
                        <i class="bi bi-pencil"></i> Edit
                    </button>
                    <button class="btn btn-danger" onclick="if(confirm('Are you sure?')) { deleteCustomer('${customer.cusID}'); viewModal.hide(); }">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </div>
            `;
            
            viewModal.show();
        })
        .catch(error => {
            console.error('Error loading customer details:', error);
            showAlert('Error loading customer details. Please try again.', 'danger');
        });
}

// Load customer data for editing
function loadCustomerForEdit(id) {
    fetch(`/api/customers/${id}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch customer for editing');
            }
            return response.json();
        })
        .then(customer => {
            document.getElementById('cusID').value = customer.cusID;
            document.getElementById('cusName').value = customer.cusName;
            document.getElementById('contactNum').value = customer.contactNum;
            document.getElementById('driverLicenseNumber').value = customer.driverLicenseNumber;
            document.getElementById('rentedVehicle').value = customer.rentedVehicle;
            document.getElementById('returnrented').checked = customer.returnrented;
            document.getElementById('nodays').value = customer.nodays || 1;
            
            document.getElementById('customerModalLabel').textContent = 'Edit Customer';
            
            customerModal.show();
        })
        .catch(error => {
            console.error('Error loading customer for edit:', error);
            showAlert('Error loading customer data. Please try again.', 'danger');
        });
}

// Delete a customer
function deleteCustomer(id) {
    if (confirm('Are you sure you want to delete this customer?')) {
        fetch(`/api/customers/${id}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to delete customer');
            }
            showAlert('Customer deleted successfully!', 'success');
            loadCustomers();
        })
        .catch(error => {
            console.error('Error deleting customer:', error);
            showAlert('Error deleting customer. Please try again.', 'danger');
        });
    }
}

// Reset the form for adding a new customer
function resetForm() {
    document.getElementById('customerForm').reset();
    document.getElementById('cusID').value = '';
}

// Handle search
function handleSearch() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    if (!searchTerm) {
        displayCustomers(customers);
        return;
    }
    
    const filteredCustomers = customers.filter(customer => 
        customer.cusID.toLowerCase().includes(searchTerm) ||
        customer.cusName.toLowerCase().includes(searchTerm) ||
        customer.contactNum.toString().includes(searchTerm)
    );
    
    displayCustomers(filteredCustomers);
}

// Handle filter change
function handleFilter() {
    const filterValue = document.getElementById('filterSelect').value;
    let filteredCustomers = [...customers];
    
    if (filterValue === 'active') {
        filteredCustomers = customers.filter(c => c.rentedVehicle > 0 && !c.returnrented);
    } else if (filterValue === 'returned') {
        filteredCustomers = customers.filter(c => c.returnrented);
    }
    
    displayCustomers(filteredCustomers);
}

// Show alert message
function showAlert(message, type) {
    const alertsContainer = document.getElementById('alerts');
    if (!alertsContainer) return;
    
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    alertsContainer.appendChild(alert);
    
    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        alert.classList.remove('show');
        setTimeout(() => {
            alertsContainer.removeChild(alert);
        }, 150);
    }, 5000);
}
