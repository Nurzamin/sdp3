package main.sender;

public class KafkaEventSender extends AbstractSimulatedSender {

    public static final String TOPIC = "notifications.events";

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || isBlank(recipient) || body == null) {
            return false;
        }
        String event = "{\"topic\":\"" + TOPIC + "\",\"key\":\"" + escape(recipient)
                + "\",\"event\":{\"title\":\"" + escape(title)
                + "\",\"body\":\"" + escape(body) + "\"}}";
        record(recipient, event);
        return true;
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    @Override
    public String getChannelName() {
        return "Kafka";
    }
}
