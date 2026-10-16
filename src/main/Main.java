package main;

import main.notification.*;
import main.sender.*;
import java.util.ArrayList;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        TelegramMessageSender telegram = new TelegramMessageSender();
        EmailMessageSender email = new EmailMessageSender();
        SmsMessageSender sms = new SmsMessageSender();
        KafkaEventSender kafka = new KafkaEventSender();
        PushNotificationSender push = new PushNotificationSender();
        for (AbstractSimulatedSender s : List.of(telegram, email, sms, kafka, push)) {
            s.setVerbose(true);
        }

        // 1. UrgentNotification + Telegram
        header("1. UrgentNotification + Telegram");
        Notification urgent = new UrgentNotification(telegram);
        urgent.send("@alice", "Server is down!");
        status(urgent);

        // 2. EncryptedNotification + Email
        header("2. EncryptedNotification + Email");
        EncryptedNotification encrypted = new EncryptedNotification(email);
        encrypted.send("bob@example.com", "Your one-time code is 482915");
        status(encrypted);
        System.out.println("   (decrypted for check: "
                + encrypted.decrypt(email.getLastDelivery().payload()
                        .split("\n\n")[1]) + ")");

        // 3. ScheduledBatchNotification + SMS  (10 recipients, 2 invalid numbers)
        header("3. ScheduledBatchNotification + SMS");
        List<String> phones = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            phones.add("+7701000000" + i);
        }
        phones.add("not-a-phone");   // will fail
        phones.add("");              // will fail
        ScheduledBatchNotification batch = new ScheduledBatchNotification(sms, phones);
        batch.send("newsletter", "Maintenance tonight at 23:00");
        System.out.println(batch.getLastSummary());

        // 4. Runtime switching: Telegram -> Email (same object, same class)
        header("4. Runtime switching: Telegram -> Email");
        Notification switching = new UrgentNotification(telegram);
        switching.send("@alice", "Deploy finished");
        switching.setSender(email);
        switching.send("alice@example.com", "Deploy finished");
        status(switching);

        // 5. Telegram unavailable -> fallback to SMS
        header("5. Telegram unavailable -> fallback to SMS");
        telegram.setAvailable(false);
        System.out.println("   Telegram available: " + telegram.isAvailable());
        Notification withFallback = new UrgentNotification(telegram, sms);
        withFallback.send("+77011234567", "Database CPU 99%");
        status(withFallback);
        telegram.setAvailable(true);

        // 6. MarketingNotification + PushNotificationSender (both added later, nothing modified)
        header("6. MarketingNotification + PushNotificationSender");
        Notification marketing = new MarketingNotification(push);
        marketing.send("device-token-42", "-30% on all plans this weekend!");
        status(marketing);
    }

    private static void header(String title) {
        System.out.println("\n=== " + title + " ===");
    }

    private static void status(Notification n) {
        System.out.println("   -> success=" + n.wasLastSendSuccessful()
                + ", delivered via " + n.getLastChannelUsed());
    }
}
