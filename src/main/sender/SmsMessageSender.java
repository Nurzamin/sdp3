package main.sender;

public class SmsMessageSender extends AbstractSimulatedSender {

    public static final int DEFAULT_MAX_LENGTH = 160;

    private final int maxLength;

    public SmsMessageSender() {
        this(DEFAULT_MAX_LENGTH);
    }

    public SmsMessageSender(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || isBlank(recipient) || body == null) {
            return false;
        }
        if (!recipient.matches("\\+\\d{10,15}")) {
            return false;                                         // invalid phone number
        }
        if (body.length() > maxLength) {
            return false;                                         // SMS length restriction
        }
        record(recipient, body);                                  // SMS has no title field
        return true;
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }
}
