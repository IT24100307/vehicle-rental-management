package discount;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoyaltyDiscount extends Discount {

    public LoyaltyDiscount() {
        super("Loyalty Discount", 10.0);
    }

    @Override
    public boolean isApplicable(String userId) {
        int rides = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("data/bookings.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(userId)) {
                    rides = Integer.parseInt(parts[1]);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rides >= 2;
    }
}
