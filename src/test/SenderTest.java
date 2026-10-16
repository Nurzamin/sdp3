package test;

import main.sender.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SenderTest {

    @Test
    void telegramSendsFormattedMessage() {
        TelegramMessageSender telegram = new TelegramMessageSender();

        assertTrue(telegram.sendMessage("@alice", "Hi", "Hello"));
        assertEquals("*Hi*\n_Hello_", telegram.getLastDelivery().payload());
    }

    @Test
    void emailContainsTitleAndSignature() {
        EmailMessageSender email = new EmailMessageSender();

        assertTrue(email.sendMessage("a@b.com", "Report", "Body text"));
        String mail = email.getLastDelivery().payload();
        assertTrue(mail.startsWith("Subject: Report"));
        assertTrue(mail.endsWith(EmailMessageSender.SIGNATURE));
    }

    @Test
    void smsAcceptsExactly160CharactersButRejects161() {
        SmsMessageSender sms = new SmsMessageSender();

        assertTrue(sms.sendMessage("+77011234567", "t", "x".repeat(160)));
        assertFalse(sms.sendMessage("+77011234567", "t", "x".repeat(161)));
        assertEquals(1, sms.getDeliveryCount());
    }

    @Test
    void smsLimitIsConfigurable() {
        SmsMessageSender sms = new SmsMessageSender(120);

        assertFalse(sms.sendMessage("+77011234567", "t", "x".repeat(121)));
        assertTrue(sms.sendMessage("+77011234567", "t", "x".repeat(120)));
    }

    @Test
    void kafkaPublishesEventWithTopicAndKey() {
        KafkaEventSender kafka = new KafkaEventSender();

        assertTrue(kafka.sendMessage("user-1", "Title", "Body \"quoted\""));
        String event = kafka.getLastDelivery().payload();
        assertTrue(event.contains(KafkaEventSender.TOPIC));
        assertTrue(event.contains("\"key\":\"user-1\""));
        assertTrue(event.contains("Body \\\"quoted\\\""));
    }

    @Test
    void unavailableSenderRejectsMessages() {
        TelegramMessageSender telegram = new TelegramMessageSender();
        telegram.setAvailable(false);

        assertFalse(telegram.isAvailable());
        assertFalse(telegram.sendMessage("@alice", "t", "b"));
        assertEquals(0, telegram.getDeliveryCount());
    }

    @Test
    void invalidRecipientIsRejected() {
        assertFalse(new EmailMessageSender().sendMessage("not-an-email", "t", "b"));
        assertFalse(new SmsMessageSender().sendMessage("12345", "t", "b"));
        assertFalse(new TelegramMessageSender().sendMessage("  ", "t", "b"));
    }
}
