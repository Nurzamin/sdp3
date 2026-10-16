package main.sender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSimulatedSender implements MessageSender {

    /** One simulated delivery (what the "provider" actually received). */
    public record Delivery(String recipient, String payload) { }

    private final List<Delivery> deliveries = new ArrayList<>();
    private boolean available = true;
    private boolean verbose = false;

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    public List<Delivery> getDeliveries() {
        return Collections.unmodifiableList(deliveries);
    }

    public int getDeliveryCount() {
        return deliveries.size();
    }

    public Delivery getLastDelivery() {
        return deliveries.isEmpty() ? null : deliveries.get(deliveries.size() - 1);
    }

    /** Stores the delivery and (optionally) prints it. */
    protected void record(String recipient, String payload) {
        deliveries.add(new Delivery(recipient, payload));
        if (verbose) {
            System.out.println("   [" + getChannelName() + "] -> " + recipient + " : "
                    + payload.replace("\n", " \\n "));
        }
    }

    protected static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
