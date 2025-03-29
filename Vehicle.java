class Vehicle{
    private int vehicleID;
    private String brand;
    private String model;
    private double rentPrice;

    public Vehicle() {
    }

    public Vehicle(int vehicleID, String brand, String model, int rentPrice){
        this.vehicleID=vehicleID;
        this.brand=brand;
        this.model=model;
        this.rentPrice=rentPrice;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void displayDetails(){
        System.out.println("Vehicle ID: "+vehicleID);
        System.out.println("Brand Name: "+getBrand());
        System.out.println("Model: "+getModel());
        System.out.println("Rent price: "+rentPrice);
    }
    public double calculateRent(int days){
        double price= rentPrice*days;
        System.out.println("Rent price with days: "+price);
        return price;
    }
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

    public Car(int vehicleID, String brand, String model,int numSeats, int rentPrice) {
        super(vehicleID, brand, model, rentPrice);
        this.numSeats=numSeats;
    }

    public Car() {
    }

    public void setHasAC(String hasAC) {
        this.hasAC=hasAC;
    }

    public String getHasAC() {
        return hasAC;
    }

    public void setTransmissionType(String transmissionType) {
        this.transmissionType = transmissionType;
    }

    public String getTransmissionType() {
        return transmissionType;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Number of Seats: "+numSeats);
        System.out.println("AC: "+getHasAC());
        System.out.println("Transmission Type: "+getTransmissionType());
    }
}

class bike extends Vehicle{
    private String bikeType;
    private int engineCapacity;

    public bike(){
    }

    public bike(int vehicleID, String brand, String model, int rentPrice) {
        super(vehicleID, brand, model, rentPrice);
    }

    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }

    public String getBikeType() {
        return bikeType;
    }

    public void setEngineCapacity(int engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    public int getEngineCapacity() {
        return engineCapacity;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Bike Type: "+getBikeType());
        System.out.println("Engine capacity: "+getEngineCapacity());
    }
}
class Van extends Vehicle{
    int cargoCapacity;
    String hasSlidingDoors;

    public Van() {
    }

    public Van(int vehicleID, String brand, String model, int rentPrice, int cargoCapacity) {
        super(vehicleID, brand, model, rentPrice);
        this.cargoCapacity = cargoCapacity;
    }

    public void setHasSlidingDoors(String hasSlidingDoors) {
        this.hasSlidingDoors = hasSlidingDoors;
    }

    public String getHasSlidingDoors() {
        return hasSlidingDoors;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Cargo Capacity: "+cargoCapacity);
        System.out.println("Door type: "+getHasSlidingDoors());
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


