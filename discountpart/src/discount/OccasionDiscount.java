package discount;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class OccasionDiscount extends Discount {
    private final List<String> specialDates = Arrays.asList("12-25", "01-01"); // MM-dd

    public OccasionDiscount() {
        super("Occasion Discount", 20.0);
    }

    @Override
    public boolean isApplicable(String userId) {
        String today = LocalDate.now().toString().substring(5); // "MM-dd"
        return specialDates.contains(today);
    }
}
