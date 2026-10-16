package main.sender;

public interface MessageSender {

    /** @return true if the message was delivered, false otherwise. */
    boolean sendMessage(String recipient, String title, String body);

    /** @return true if the provider can accept messages right now. */
    boolean isAvailable();

    String getChannelName();
}
