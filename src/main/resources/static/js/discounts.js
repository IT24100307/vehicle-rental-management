document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const discountForm = document.getElementById('discountForm');
    const discountTypeSelect = document.getElementById('discountType');
    const seasonalFields = document.querySelectorAll('.seasonal-fields');
    const loyaltyFields = document.querySelectorAll('.loyalty-fields');
    const resetFormButton = document.getElementById('resetForm');
    const refreshDiscountsButton = document.getElementById('refreshDiscounts');
    const initDiscountsButton = document.getElementById('initDiscounts');
    const discountsList = document.getElementById('discountsList');
    const loadingDiscounts = document.getElementById('loadingDiscounts');
    const noDiscounts = document.getElementById('noDiscounts');
    const filterDiscountType = document.getElementById('filterDiscountType');
    const searchDiscount = document.getElementById('searchDiscount');
    const deleteDiscountModal = new bootstrap.Modal(document.getElementById('deleteDiscountModal'));
    const confirmDeleteButton = document.getElementById('confirmDelete');
    const discountToast = new bootstrap.Toast(document.getElementById('discountToast'));
    const toastTitle = document.getElementById('toastTitle');
    const toastMessage = document.getElementById('toastMessage');
    
    // Track current discount being edited or deleted
    let currentDiscountId = null;
    let allDiscounts = [];
    
    // Load discounts on page load
    loadDiscounts();
    
    // Event Listeners
    discountTypeSelect.addEventListener('change', toggleConditionalFields);
    discountForm.addEventListener('submit', handleDiscountFormSubmit);
    resetFormButton.addEventListener('click', resetForm);
    refreshDiscountsButton.addEventListener('click', loadDiscounts);
    initDiscountsButton.addEventListener('click', initializeDefaultDiscounts);
    filterDiscountType.addEventListener('change', filterDiscounts);
    searchDiscount.addEventListener('input', filterDiscounts);
    confirmDeleteButton.addEventListener('click', deleteDiscount);
    
    // Functions
    function toggleConditionalFields() {
        const discountType = discountTypeSelect.value;
        
        // Hide all conditional fields first
        seasonalFields.forEach(field => field.classList.add('d-none'));
        loyaltyFields.forEach(field => field.classList.add('d-none'));
        
        // Show fields based on discount type
        if (discountType === 'SEASONAL') {
            seasonalFields.forEach(field => field.classList.remove('d-none'));
            
            // Make date fields required for seasonal discounts
            document.getElementById('startDate').setAttribute('required', '');
            document.getElementById('endDate').setAttribute('required', '');
            document.getElementById('minimumRides').removeAttribute('required');
        } else if (discountType === 'LOYALTY') {
            loyaltyFields.forEach(field => field.classList.remove('d-none'));
            
            // Make minimum rides required for loyalty discounts
            document.getElementById('minimumRides').setAttribute('required', '');
            document.getElementById('startDate').removeAttribute('required');
            document.getElementById('endDate').removeAttribute('required');
        } else {
            // Remove required attributes for other discount types
            document.getElementById('startDate').removeAttribute('required');
            document.getElementById('endDate').removeAttribute('required');
            document.getElementById('minimumRides').removeAttribute('required');
        }
    }
    
    function handleDiscountFormSubmit(event) {
        event.preventDefault();
        
        // Form validation
        if (!discountForm.checkValidity()) {
            event.stopPropagation();
            discountForm.classList.add('was-validated');
            return;
        }
        
        // Collect form data
        const discount = {
            name: document.getElementById('discountName').value,
            description: document.getElementById('discountDescription').value,
            discountPercentage: parseFloat(document.getElementById('discountPercentage').value),
            discountType: document.getElementById('discountType').value,
            active: document.getElementById('isActive').checked,
            applyToAllVehicles: document.getElementById('applyToAllVehicles').checked
        };
        
        // Add conditional fields based on discount type
        if (discount.discountType === 'SEASONAL') {
            discount.startDate = document.getElementById('startDate').value;
            discount.endDate = document.getElementById('endDate').value;
        } else if (discount.discountType === 'LOYALTY') {
            discount.minimumRides = parseInt(document.getElementById('minimumRides').value);
        }
        
        // Determine if this is a create or update operation
        const discountId = document.getElementById('discountId').value;
        let url = '/api/discounts';
        let method = 'POST';
        
        if (discountId) {
            url = `/api/discounts/${discountId}`;
            method = 'PUT';
        }
        
        // Send API request
        fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(discount)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to save discount');
            }
            return response.json();
        })
        .then(data => {
            // Show success notification
            showToast('Success', `Discount ${discountId ? 'updated' : 'created'} successfully`);
            
            // Reset form and refresh discounts list
            resetForm();
            loadDiscounts();
            
            // Switch to list tab
            document.getElementById('list-tab').click();
        })
        .catch(error => {
            showToast('Error', error.message, true);
        });
    }
    
    function resetForm() {
        discountForm.reset();
        document.getElementById('discountId').value = '';
        discountForm.classList.remove('was-validated');
        
        // Reset conditional fields
        seasonalFields.forEach(field => field.classList.add('d-none'));
        loyaltyFields.forEach(field => field.classList.add('d-none'));
        
        // Remove required attributes
        document.getElementById('startDate').removeAttribute('required');
        document.getElementById('endDate').removeAttribute('required');
        document.getElementById('minimumRides').removeAttribute('required');
    }
    
    function loadDiscounts() {
        // Show loading state
        loadingDiscounts.classList.remove('d-none');
        noDiscounts.classList.add('d-none');
        
        // Clear existing discount cards
        const existingCards = discountsList.querySelectorAll('.discount-card-container');
        existingCards.forEach(card => card.remove());
        
        // Fetch discounts from API
        fetch('/api/discounts')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load discounts');
                }
                return response.json();
            })
            .then(discounts => {
                // Hide loading state
                loadingDiscounts.classList.add('d-none');
                
                // Store all discounts for filtering
                allDiscounts = discounts;
                
                // Check if there are any discounts
                if (discounts.length === 0) {
                    noDiscounts.classList.remove('d-none');
                } else {
                    // Render discount cards
                    renderDiscountCards(discounts);
                }
            })
            .catch(error => {
                loadingDiscounts.classList.add('d-none');
                showToast('Error', error.message, true);
            });
    }
    
    function renderDiscountCards(discounts) {
        discounts.forEach(discount => {
            // Create discount card
            const cardHtml = createDiscountCardHtml(discount);
            
            // Append to list
            discountsList.insertAdjacentHTML('beforeend', cardHtml);
            
            // Add event listeners to card buttons
            const cardContainer = discountsList.lastElementChild;
            const editButton = cardContainer.querySelector('.edit-discount');
            const deleteButton = cardContainer.querySelector('.delete-discount');
            const toggleButton = cardContainer.querySelector('.toggle-discount');
            
            if (editButton) {
                editButton.addEventListener('click', () => editDiscount(discount));
            }
            
            if (deleteButton) {
                deleteButton.addEventListener('click', () => showDeleteConfirmation(discount.id));
            }
            
            if (toggleButton) {
                toggleButton.addEventListener('click', () => toggleDiscountStatus(discount.id));
            }
        });
    }
    
    function createDiscountCardHtml(discount) {
        // Determine badge class based on discount type
        let badgeClass = '';
        let badgeIcon = '';
        
        switch (discount.discountType) {
            case 'SEASONAL':
                badgeClass = 'bg-info';
                badgeIcon = 'fa-calendar-alt';
                break;
            case 'LOYALTY':
                badgeClass = 'bg-warning';
                badgeIcon = 'fa-medal';
                break;
            case 'GLOBAL':
                badgeClass = 'bg-success';
                badgeIcon = 'fa-globe';
                break;
            default:
                badgeClass = 'bg-secondary';
                badgeIcon = 'fa-tag';
        }
        
        // Format dates if available
        let dateInfo = '';
        if (discount.discountType === 'SEASONAL' && discount.startDate && discount.endDate) {
            const startDate = new Date(discount.startDate).toLocaleDateString();
            const endDate = new Date(discount.endDate).toLocaleDateString();
            dateInfo = `<p class="discount-dates">Valid from ${startDate} to ${endDate}</p>`;
        }
        
        // Format minimum rides if available
        let minimumRidesInfo = '';
        if (discount.discountType === 'LOYALTY' && discount.minimumRides) {
            minimumRidesInfo = `<p class="discount-rides">Minimum ${discount.minimumRides} rides required</p>`;
        }
        
        // Create card HTML
        return `
            <div class="col-md-4 discount-card-container">
                <div class="card discount-card ${discount.active ? '' : 'bg-light text-muted'}">
                    <div class="card-body position-relative">
                        <span class="badge ${badgeClass} discount-type-badge">
                            <i class="fas ${badgeIcon}"></i> ${discount.discountType}
                        </span>
                        <span class="badge bg-danger discount-badge">
                            ${discount.discountPercentage}%
                        </span>
                        <h5 class="card-title mt-4">
                            <span class="active-indicator active-${discount.active}"></span>
                            ${discount.name}
                        </h5>
                        <p class="card-text">${discount.description}</p>
                        ${dateInfo}
                        ${minimumRidesInfo}
                        ${discount.applyToAllVehicles ? '<p><span class="badge bg-primary">Applies to All Vehicles</span></p>' : ''}
                        <div class="btn-group w-100 mt-3">
                            <button class="btn btn-outline-primary btn-sm edit-discount">
                                <i class="fas fa-edit"></i> Edit
                            </button>
                            <button class="btn btn-outline-${discount.active ? 'warning' : 'success'} btn-sm toggle-discount">
                                <i class="fas ${discount.active ? 'fa-pause' : 'fa-play'}"></i> 
                                ${discount.active ? 'Deactivate' : 'Activate'}
                            </button>
                            <button class="btn btn-outline-danger btn-sm delete-discount">
                                <i class="fas fa-trash"></i> Delete
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }
    
    function editDiscount(discount) {
        // Populate form with discount data
        document.getElementById('discountId').value = discount.id;
        document.getElementById('discountName').value = discount.name;
        document.getElementById('discountDescription').value = discount.description;
        document.getElementById('discountPercentage').value = discount.discountPercentage;
        document.getElementById('discountType').value = discount.discountType;
        document.getElementById('isActive').checked = discount.active;
        document.getElementById('applyToAllVehicles').checked = discount.applyToAllVehicles;
        
        // Handle conditional fields
        if (discount.discountType === 'SEASONAL') {
            document.getElementById('startDate').value = discount.startDate;
            document.getElementById('endDate').value = discount.endDate;
        } else if (discount.discountType === 'LOYALTY') {
            document.getElementById('minimumRides').value = discount.minimumRides;
        }
        
        // Update conditional fields visibility
        toggleConditionalFields();
        
        // Switch to create/edit tab
        document.getElementById('create-tab').click();
    }
    
    function showDeleteConfirmation(discountId) {
        currentDiscountId = discountId;
        deleteDiscountModal.show();
    }
    
    function deleteDiscount() {
        if (!currentDiscountId) return;
        
        fetch(`/api/discounts/${currentDiscountId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to delete discount');
            }
            return response.json();
        })
        .then(data => {
            deleteDiscountModal.hide();
            showToast('Success', 'Discount deleted successfully');
            loadDiscounts();
        })
        .catch(error => {
            deleteDiscountModal.hide();
            showToast('Error', error.message, true);
        });
    }
    
    function toggleDiscountStatus(discountId) {
        fetch(`/api/discounts/${discountId}/toggle`, {
            method: 'PUT'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to update discount status');
            }
            return response.json();
        })
        .then(data => {
            showToast('Success', `Discount ${data.active ? 'activated' : 'deactivated'} successfully`);
            loadDiscounts();
        })
        .catch(error => {
            showToast('Error', error.message, true);
        });
    }
    
    function initializeDefaultDiscounts() {
        fetch('/api/discounts/initialize', {
            method: 'POST'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to initialize default discounts');
            }
            return response.json();
        })
        .then(data => {
            showToast('Success', data.message);
            loadDiscounts();
        })
        .catch(error => {
            showToast('Error', error.message, true);
        });
    }
    
    function filterDiscounts() {
        const searchTerm = searchDiscount.value.toLowerCase();
        const filterType = filterDiscountType.value;
        
        // Filter discounts based on search term and type
        const filteredDiscounts = allDiscounts.filter(discount => {
            const matchesSearch = 
                discount.name.toLowerCase().includes(searchTerm) ||
                discount.description.toLowerCase().includes(searchTerm);
                
            const matchesType = filterType ? discount.discountType === filterType : true;
            
            return matchesSearch && matchesType;
        });
        
        // Clear existing discount cards
        const existingCards = discountsList.querySelectorAll('.discount-card-container');
        existingCards.forEach(card => card.remove());
        
        // Hide loading and no discounts messages
        loadingDiscounts.classList.add('d-none');
        
        // Show no results message if needed
        if (filteredDiscounts.length === 0) {
            noDiscounts.classList.remove('d-none');
            noDiscounts.querySelector('p').textContent = 'No discounts match your search criteria.';
        } else {
            noDiscounts.classList.add('d-none');
            renderDiscountCards(filteredDiscounts);
        }
    }
    
    function showToast(title, message, isError = false) {
        toastTitle.textContent = title;
        toastMessage.textContent = message;
        
        // Set toast color based on error status
        const toastElement = document.getElementById('discountToast');
        toastElement.classList.remove('bg-danger', 'text-white');
        
        if (isError) {
            toastElement.classList.add('bg-danger', 'text-white');
        }
        
        discountToast.show();
    }
}); 