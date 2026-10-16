package main.notification;

import main.sender.MessageSender;

import java.util.Objects;

public abstract class Notification {

    protected MessageSender sender;

    private boolean lastSendSuccessful;
    private String lastChannelUsed;

    protected Notification(MessageSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender must not be null");
    }

    /** Runtime provider switching - no change to the Notification class is needed. */
    public void setSender(MessageSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender must not be null");
    }

    public MessageSender getSender() {
        return sender;
    }

    public abstract void send(String recipient, String message);

    /** Sends through the given sender and remembers the outcome. */
    protected boolean deliver(MessageSender target, String recipient, String title, String body) {
        boolean ok = target.sendMessage(recipient, title, body);
        setLastResult(ok, ok ? target.getChannelName() : null);
        return ok;
    }

    protected void setLastResult(boolean successful, String channel) {
        this.lastSendSuccessful = successful;
        this.lastChannelUsed = channel;
    }

    public boolean wasLastSendSuccessful() {
        return lastSendSuccessful;
    }

    /** Channel that actually delivered the last message (null if it failed). */
    public String getLastChannelUsed() {
        return lastChannelUsed;
    }
}
