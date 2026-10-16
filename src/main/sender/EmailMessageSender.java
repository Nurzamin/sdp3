package main.sender;

public class EmailMessageSender extends AbstractSimulatedSender {

    public static final String SIGNATURE = "-- \nNotification Gateway Team";

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || isBlank(recipient) || body == null) {
            return false;
        }
        if (!recipient.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return false;                                         // invalid e-mail address
        }
        String mail = "Subject: " + title + "\n\n" + body + "\n\n" + SIGNATURE;
        record(recipient, mail);
        return true;
    }

    @Override
    public String getChannelName() {
        return "Email";
    }
}
