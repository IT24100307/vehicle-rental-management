package discount;

import java.util.ArrayList;
import java.util.List;

public class DiscountManager {
    private final List<Discount> discounts;

    public DiscountManager() {
        discounts = new ArrayList<>();
        discounts.add(new OccasionDiscount());
        discounts.add(new LoyaltyDiscount());
    }

    public double calculateTotalDiscount(String userId) {
        double totalDiscount = 0;
        for (Discount discount : discounts) {
            if (discount.isApplicable(userId)) {
                totalDiscount += discount.getPercentage();
                System.out.println("Applied: " + discount.getReason());
            }
        }
        return totalDiscount;
    }
}
