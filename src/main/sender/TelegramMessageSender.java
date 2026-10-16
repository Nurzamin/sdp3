package main.sender;

public class TelegramMessageSender extends AbstractSimulatedSender {

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || isBlank(recipient) || body == null) {
            return false;
        }
        String formatted = "*" + title + "*\n_" + body + "_";   // Markdown: bold title, italic body
        record(recipient, formatted);
        return true;
    }

    @Override
    public String getChannelName() {
        return "Telegram";
    }
}
