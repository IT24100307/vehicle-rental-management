package discount;

public abstract class Discount {
    protected String reason;
    protected double percentage;

    public Discount(String reason, double percentage) {
        this.reason = reason;
        this.percentage = percentage;
    }

    public String getReason() {
        return reason;
    }

    public double getPercentage() {
        return percentage;
    }

    // Polymorphic method
    public abstract boolean isApplicable(String userId);
}
