package test;

import main.notification.*;
import main.sender.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    private TelegramMessageSender telegram;
    private EmailMessageSender email;
    private SmsMessageSender sms;
    private KafkaEventSender kafka;

    @BeforeEach
    void setUp() {
        telegram = new TelegramMessageSender();
        email = new EmailMessageSender();
        sms = new SmsMessageSender();
        kafka = new KafkaEventSender();
    }

    // ---------- successful delivery ----------

    @Test
    void urgentAddsPrefixAndDeliversThroughTelegram() {
        Notification n = new UrgentNotification(telegram);

        n.send("@alice", "Server down");

        assertTrue(n.wasLastSendSuccessful());
        assertEquals("Telegram", n.getLastChannelUsed());
        assertTrue(telegram.getLastDelivery().payload().contains("[URGENT] Server down"));
    }

    // ---------- unavailable provider / failure ----------

    @Test
    void urgentFailsWhenNoProviderIsAvailable() {
        telegram.setAvailable(false);
        sms.setAvailable(false);
        Notification n = new UrgentNotification(telegram, sms);

        n.send("+77011234567", "Anything");

        assertFalse(n.wasLastSendSuccessful());
        assertNull(n.getLastChannelUsed());
        assertEquals(0, telegram.getDeliveryCount());
        assertEquals(0, sms.getDeliveryCount());
    }

    @Test
    void sendToInvalidRecipientIsReportedAsFailure() {
        Notification n = new EncryptedNotification(email);

        n.send("not-an-email", "secret");

        assertFalse(n.wasLastSendSuccessful());
        assertEquals(0, email.getDeliveryCount());
    }

    // ---------- fallback ----------

    @Test
    void urgentFallsBackToSmsWhenTelegramIsUnavailable() {
        telegram.setAvailable(false);
        Notification n = new UrgentNotification(telegram, sms);

        n.send("+77011234567", "DB down");

        assertTrue(n.wasLastSendSuccessful());
        assertEquals("SMS", n.getLastChannelUsed());
        assertEquals(0, telegram.getDeliveryCount());
        assertEquals("[URGENT] DB down", sms.getLastDelivery().payload());
    }

    @Test
    void urgentFallsBackWhenPrimaryIsUpButRejectsTheMessage() {
        // SMS is available but the text is too long -> next provider in the chain is used
        Notification n = new UrgentNotification(sms, email);

        n.send("+77011234567", "x".repeat(200));      // SMS rejects (and phone is not an e-mail ->
        assertFalse(n.wasLastSendSuccessful());       // email rejects too) -> overall failure

        n.send("a@b.com", "x".repeat(200));           // SMS rejects (bad phone), email accepts
        assertTrue(n.wasLastSendSuccessful());
        assertEquals("Email", n.getLastChannelUsed());
    }

    @Test
    void fallbackWorksWithAnyMessageSenderImplementation() {
        // A custom provider written inside the test: Urgent knows nothing about it
        List<String> log = new ArrayList<>();
        MessageSender custom = new MessageSender() {
            public boolean sendMessage(String r, String t, String b) { return log.add(r + ":" + b); }
            public boolean isAvailable() { return true; }
            public String getChannelName() { return "Custom"; }
        };
        telegram.setAvailable(false);
        Notification n = new UrgentNotification(telegram, custom);

        n.send("user", "hello");

        assertEquals("Custom", n.getLastChannelUsed());
        assertEquals(List.of("user:[URGENT] hello"), log);
    }

    // ---------- SMS length restriction through the abstraction ----------

    @Test
    void longUrgentMessageIsNotDeliveredBySmsAlone() {
        Notification n = new UrgentNotification(sms);

        n.send("+77011234567", "x".repeat(160));      // + "[URGENT] " prefix -> 169 chars

        assertFalse(n.wasLastSendSuccessful());
        assertEquals(0, sms.getDeliveryCount());
    }

    // ---------- encrypted message ----------

    @Test
    void encryptedNotificationSendsTransformedMessage() {
        EncryptedNotification n = new EncryptedNotification(email);

        n.send("bob@example.com", "code 1234");

        String mail = email.getLastDelivery().payload();
        assertFalse(mail.contains("code 1234"));                    // plain text is NOT in the mail
        String cipher = mail.split("\n\n")[1];
        assertEquals(n.encrypt("code 1234"), cipher);
        assertEquals("code 1234", n.decrypt(cipher));               // round trip
    }

    // ---------- batch ----------

    @Test
    void batchProducesTotalSuccessfulFailedSummary() {
        List<String> recipients = new ArrayList<>();
        for (int i = 1; i <= 8; i++) recipients.add("+7701000000" + i);
        recipients.add("bad-number");
        recipients.add("");
        ScheduledBatchNotification batch = new ScheduledBatchNotification(sms, recipients);

        BatchSummary summary = batch.sendBatch("Hello all");

        assertEquals(new BatchSummary(10, 8, 2), summary);
        assertEquals("Total: 10\nSuccessful: 8\nFailed: 2", summary.toString());
        assertEquals(8, sms.getDeliveryCount());
        assertFalse(batch.wasLastSendSuccessful());                 // not everyone got it
    }

    @Test
    void batchSendViaBaseClassMethodDeliversToAllRecipients() {
        ScheduledBatchNotification batch = new ScheduledBatchNotification(
                kafka, List.of("u1", "u2", "u3"));

        batch.send("label", "news");

        assertEquals(3, kafka.getDeliveryCount());
        assertTrue(batch.wasLastSendSuccessful());
    }

    // ---------- runtime sender switching ----------

    @Test
    void sameNotificationObjectCanSwitchProviderAtRuntime() {
        Notification n = new UrgentNotification(telegram);

        n.send("@alice", "first");
        n.setSender(email);
        n.send("alice@example.com", "second");

        assertEquals(1, telegram.getDeliveryCount());
        assertEquals(1, email.getDeliveryCount());
        assertEquals("Email", n.getLastChannelUsed());
    }

    @Test
    void settingNullSenderIsRejected() {
        Notification n = new UrgentNotification(telegram);

        assertThrows(NullPointerException.class, () -> n.setSender(null));
    }
}
