import discount.DiscountManager;

public class Main {
    public static void main(String[] args) {
        String userId = "user123"; // change as needed
        DiscountManager manager = new DiscountManager();
        double discount = manager.calculateTotalDiscount(userId);
        System.out.println("Total discount for " + userId + ": " + discount + "%");
    }
}
