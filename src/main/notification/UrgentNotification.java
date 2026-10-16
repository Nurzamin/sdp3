package main.notification;

import main.sender.*;

import java.util.ArrayList;
import java.util.List;

public class UrgentNotification extends Notification {

    public static final String PREFIX = "[URGENT] ";
    public static final String TITLE = "URGENT";

    private final List<MessageSender> fallbackSenders = new ArrayList<>();

    public UrgentNotification(MessageSender primary, MessageSender... fallbacks) {
        super(primary);
        for (MessageSender fallback : fallbacks) {
            addFallbackSender(fallback);
        }
    }

    public void addFallbackSender(MessageSender fallback) {
        fallbackSenders.add(java.util.Objects.requireNonNull(fallback));
    }

    @Override
    public void send(String recipient, String message) {
        String body = PREFIX + message;

        List<MessageSender> chain = new ArrayList<>();
        chain.add(sender);               // primary first
        chain.addAll(fallbackSenders);   // then fallbacks in order

        for (MessageSender candidate : chain) {
            if (!candidate.isAvailable()) {
                continue;                // provider is down -> try the next one
            }
            if (deliver(candidate, recipient, TITLE, body)) {
                return;                  // delivered
            }
            // provider is up but rejected the message (e.g. SMS too long) -> try the next one
        }
        setLastResult(false, null);      // nobody could deliver
    }
}
