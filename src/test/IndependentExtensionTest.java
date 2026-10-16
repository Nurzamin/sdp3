package test;

import main.sender.*;
import main.notification.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IndependentExtensionTest {

    @Test
    void newPushSenderWorksWithAllExistingNotifications() {
        PushNotificationSender push = new PushNotificationSender();

        new UrgentNotification(push).send("token-1", "alert");
        new EncryptedNotification(push).send("token-2", "secret");
        new ScheduledBatchNotification(push, List.of("t3", "t4")).send("batch", "news");

        assertEquals(4, push.getDeliveryCount());
        assertTrue(push.getDeliveries().get(0).payload().contains("[URGENT] alert"));
    }

    @Test
    void pushTruncatesLongBodyInsteadOfFailing() {
        PushNotificationSender push = new PushNotificationSender();
        Notification n = new UrgentNotification(push);

        n.send("token", "x".repeat(500));

        assertTrue(n.wasLastSendSuccessful());
        assertTrue(push.getLastDelivery().payload().contains("..."));
    }

    @Test
    void newMarketingNotificationWorksWithAllExistingSenders() {
        List<AbstractSimulatedSender> senders = List.of(
                new TelegramMessageSender(), new EmailMessageSender(),
                new SmsMessageSender(), new KafkaEventSender());
        String[] recipients = {"@user", "user@mail.com", "+77011234567", "user-1"};

        for (int i = 0; i < senders.size(); i++) {
            Notification n = new MarketingNotification(senders.get(i));
            n.send(recipients[i], "-20% today!");

            assertTrue(n.wasLastSendSuccessful(), senders.get(i).getChannelName());
            assertTrue(senders.get(i).getLastDelivery().payload().contains("[PROMO] -20% today!"));
        }
    }

    @Test
    void marketingPlusPushWorksTogether() {
        PushNotificationSender push = new PushNotificationSender();
        Notification n = new MarketingNotification(push);

        n.send("device-42", "Weekend sale");

        assertTrue(n.wasLastSendSuccessful());
        assertEquals("Push", n.getLastChannelUsed());
        assertTrue(push.getLastDelivery().payload().contains("Reply STOP"));
    }

    @Test
    void marketingFailsOnSmsWhenTextIsTooLong() {
        Notification n = new MarketingNotification(new SmsMessageSender());

        n.send("+77011234567", "y".repeat(150));      // + promo prefix and footer > 160

        assertFalse(n.wasLastSendSuccessful());
    }
}
