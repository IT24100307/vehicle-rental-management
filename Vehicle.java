class Vehicle{
    private int vehicleID;
    private String brand;
    private String model;
    private double rentPrice; //default price

    //default constructor
    public Vehicle() {
    }
    //constructor with peramiter
    public Vehicle(int vehicleID, String brand, String model, int rentPrice){
        this.vehicleID=vehicleID;
        this.brand=brand;
        this.model=model;
        this.rentPrice=rentPrice;
    }
    //displaying all details
    public void displayDetails(){
        System.out.println("Vehicle ID: "+vehicleID);
        System.out.println("Brand Name: "+brand);
        System.out.println("Model: "+model);
        System.out.println("Rent price: "+rentPrice);
    }
    //calculating price with days
    public double calculateRent(int days){
        double price= rentPrice*days;
        System.out.println("Rent price with days: "+price);
        return price;
    }
    //check availability
    public void isAvailable(boolean status) {
        if(status==true){
            System.out.println("Vehicle is Available");
            return;
        }
        if(status==false){
            System.out.println("Vehicle is not available");
            return;
        }
    }
}
class Car extends Vehicle{
    private int numSeats;
    private String hasAC;
    private String transmissionType;

    public Car(int vehicleID, String brand, String model, int rentPrice, int numSeats, String hasAC, String transmissionType) {
        super(vehicleID, brand, model, rentPrice);
        this.numSeats = numSeats;
        this.hasAC = hasAC;
        this.transmissionType = transmissionType;
    }

    public Car() {
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Number of Seats: "+numSeats);
        System.out.println("AC: "+hasAC);
        System.out.println("Transmission Type: "+transmissionType);
    }
}

class bike extends Vehicle{
    private String bikeType;
    private int engineCapacity;

    public bike(){
    }

    public bike(int vehicleID, String brand, String model, int rentPrice, String bikeType, int engineCapacity) {
        super(vehicleID, brand, model, rentPrice);
        this.bikeType = bikeType;
        this.engineCapacity = engineCapacity;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Bike Type: "+bikeType);
        System.out.println("Engine capacity: "+engineCapacity);
    }
}
class Van extends Vehicle{
    int cargoCapacity;
    String hasSlidingDoors;

    public Van() {
    }

    public Van(int vehicleID, String brand, String model, int rentPrice, int cargoCapacity, String hasSlidingDoors) {
        super(vehicleID, brand, model, rentPrice);
        this.cargoCapacity = cargoCapacity;
        this.hasSlidingDoors = hasSlidingDoors;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Cargo Capacity: "+cargoCapacity);
        System.out.println("Door type: "+hasSlidingDoors);
    }
}

class Lorry extends Vehicle{
    int maxLoadCapacity;
    int numAxles; //have at least two axles

    public Lorry() {
    }

    public Lorry(int vehicleID, String brand, String model, int rentPrice, int maxLoadCapacity, int numAxles) {
        super(vehicleID, brand, model, rentPrice);
        this.maxLoadCapacity = maxLoadCapacity;
        this.numAxles = numAxles;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Max load capacity: "+maxLoadCapacity);
        System.out.println("Number of axles: "+numAxles);
    }
}
class Bus extends Vehicle{
    int passengerCapacity;
    String hasWheelchairAccess;

    public Bus() {
    }

    public Bus(int vehicleID, String brand, String model, int rentPrice, int passengerCapacity, String hasWheelchairAccess) {
        super(vehicleID, brand, model, rentPrice);
        this.passengerCapacity = passengerCapacity;
        this.hasWheelchairAccess = hasWheelchairAccess;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Passenger Capacity: "+passengerCapacity);
        System.out.println("Wheel chair Access: "+hasWheelchairAccess);
    }
}


