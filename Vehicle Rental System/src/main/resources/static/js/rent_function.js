// Initialize rent vehicle modal
function initRentVehicleModal() {
    const form = document.getElementById('rentVehicleForm');
    
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        if (!selectedVehicle) {
            console.error('No vehicle selected');
            alert('No vehicle selected');
            return;
        }
        
        const customer = JSON.parse(localStorage.getItem('customer'));
        if (!customer) {
            console.error('No customer logged in');
            alert('Please log in again');
            window.location.href = 'login.html';
            return;
        }
        
        const days = parseInt(document.getElementById('rentalDays').value) || 1;
        
        // Ensure we have a valid vehicle ID
        if (!selectedVehicle.vehicleID) {
            console.error('Selected vehicle has no ID');
            alert('Invalid vehicle data. Please try selecting a different vehicle.');
            return;
        }
        
        console.log('Attempting to rent vehicle:', {
            customerId: customer.customerId,
            vehicleId: selectedVehicle.vehicleID,
            days: days,
            selectedVehicle: selectedVehicle
        });
        
        try {
            // Show loading state
            const submitBtn = form.querySelector('button[type="submit"]');
            submitBtn.textContent = 'Confirming...';
            submitBtn.disabled = true;
            
            // Make direct fetch call for better control
            const url = `/api/customers/${encodeURIComponent(customer.customerId)}/rent/${encodeURIComponent(selectedVehicle.vehicleID)}?days=${encodeURIComponent(days)}`;
            console.log('Direct rental request URL:', url);
            
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            console.log('Raw response status:', response.status, response.statusText);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('Error response body:', errorText);
                throw new Error(`Request failed: ${response.status} ${response.statusText}`);
            }
            
            const data = await response.json();
            console.log('Rental response data:', data);
            
            // Reset form
            form.reset();
            
            // Hide modal
            document.getElementById('rentVehicleModal').style.display = 'none';
            
            // Show success message
            alert(`You have successfully rented the ${selectedVehicle.brand} ${selectedVehicle.model} for ${days} days.`);
            
            // Reload vehicles and rented vehicles
            loadVehicles();
            loadRentedVehicles();
            
        } catch (error) {
            console.error('Error renting vehicle:', error);
            alert('Failed to rent vehicle: ' + (error.message || 'Unknown error'));
        } finally {
            // Reset button
            const submitBtn = form.querySelector('button[type="submit"]');
            submitBtn.textContent = 'Confirm Rental';
            submitBtn.disabled = false;
        }
    });
}
