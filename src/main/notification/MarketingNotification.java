package main.notification;

import main.sender.*;

public class MarketingNotification extends Notification {

    public static final String TITLE = "Special offer";
    public static final String PROMO_PREFIX = "[PROMO] ";
    public static final String UNSUBSCRIBE_FOOTER = " Reply STOP to unsubscribe.";

    public MarketingNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    public void send(String recipient, String message) {
        deliver(sender, recipient, TITLE, PROMO_PREFIX + message + UNSUBSCRIBE_FOOTER);
    }
}
