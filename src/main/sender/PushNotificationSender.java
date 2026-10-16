package main.sender;


public class PushNotificationSender extends AbstractSimulatedSender {

    public static final int MAX_BODY_PREVIEW = 100;

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || isBlank(recipient) || body == null) {
            return false;
        }
        String preview = body.length() > MAX_BODY_PREVIEW
                ? body.substring(0, MAX_BODY_PREVIEW - 3) + "..."
                : body;
        record(recipient, "PUSH{title='" + title + "', body='" + preview + "'}");
        return true;
    }

    @Override
    public String getChannelName() {
        return "Push";
    }
}
