class Customer {
    private String cusID;
    private String cusName;
    private int contactNum;
    private int driverLicenseNumber;
    private int rentedVehicle;
    int nodays;
    boolean returnrented;

    public Customer(String cusID, String cusName, int contactNum, int driverLicenseNumber, int rentedVehicle) {
        this.cusID = cusID;
        this.cusName = cusName;
        this.contactNum = contactNum;
        this.driverLicenseNumber = driverLicenseNumber;
        this.rentedVehicle = rentedVehicle;
    }

    public Customer(String cusID, String cusName, int contactNum, int rentedVehicle) {
        this.cusID = cusID;
        this.cusName = cusName;
        this.contactNum = contactNum;
        this.rentedVehicle = rentedVehicle;
    }

    public Customer() {
    }

    public void displaydetails(Vehicle V){
        System.out.println("Customer Name : "+cusName);
        System.out.println("Customer ID : " +cusID);
        System.out.println("License Number : "+driverLicenseNumber);
        System.out.println("Vehicle ID : " +rentedVehicle);
        V.isAvailable(rentedVehicle);
        V.displayDetails();
    }

    public void rentVehicle(Vehicle V,int days){
        V=new Vehicle(rentedVehicle);
        this.nodays=days;
    }

    public int getContactNum() {
        return contactNum;
    }

    public int getNodays() {
        return nodays;
    }

    public void returnVehicle() {
        if (returnrented){
            Vehicle V = new Vehicle(rentedVehicle);
        }
        else{
            System.out.println("Return the Vehicle after " +nodays);
        }
    }
}
