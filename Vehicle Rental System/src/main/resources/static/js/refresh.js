// Function to refresh the inventory
function refreshInventory() {
    loadVehicles();
    loadRentedVehicles();
    showToast('Inventory refreshed');
}

// Helper function to show a toast message
function showToast(message) {
    // Create toast element if it doesn't exist
    let toast = document.getElementById('toast-notification');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast-notification';
        toast.className = 'toast-notification';
        document.body.appendChild(toast);
    }
    
    // Set message and show the toast
    toast.textContent = message;
    toast.classList.add('show');
    
    // Hide after 3 seconds
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// Initialize event listeners for refresh button when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Add refresh button listener (in addition to existing listeners)
    const refreshBtn = document.getElementById('refreshInventoryBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            refreshInventory();
        });
    }
});
