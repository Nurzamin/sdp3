package main.notification;

import main.sender.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScheduledBatchNotification extends Notification {

    public static final String TITLE = "Scheduled notification";

    private final List<String> recipients;
    private BatchSummary lastSummary;

    public ScheduledBatchNotification(MessageSender sender, List<String> recipients) {
        super(sender);
        this.recipients = new ArrayList<>(Objects.requireNonNull(recipients));
    }

    /** @param batchLabel only a label of the batch (e.g. "newsletter"); real targets are the stored recipients. */
    @Override
    public void send(String batchLabel, String message) {
        sendBatch(message);
    }

    public BatchSummary sendBatch(String message) {
        int ok = 0;
        for (String recipient : recipients) {
            if (deliver(sender, recipient, TITLE, message)) {
                ok++;
            }
        }
        lastSummary = new BatchSummary(recipients.size(), ok, recipients.size() - ok);
        setLastResult(lastSummary.failed() == 0, sender.getChannelName());
        return lastSummary;
    }

    public BatchSummary getLastSummary() {
        return lastSummary;
    }

    public List<String> getRecipients() {
        return List.copyOf(recipients);
    }
}
