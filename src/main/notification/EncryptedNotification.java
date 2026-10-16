package main.notification;

import main.sender.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptedNotification extends Notification {

    public static final String TITLE = "Encrypted message";
    private static final String DEFAULT_KEY = "bridge";

    private final String key;

    public EncryptedNotification(MessageSender sender) {
        this(sender, DEFAULT_KEY);
    }

    public EncryptedNotification(MessageSender sender, String key) {
        super(sender);
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty");
        }
        this.key = key;
    }

    @Override
    public void send(String recipient, String message) {
        deliver(sender, recipient, TITLE, encrypt(message));
    }

    public String encrypt(String plain) {
        return Base64.getEncoder().encodeToString(xor(plain.getBytes(StandardCharsets.UTF_8)));
    }

    public String decrypt(String cipher) {
        return new String(xor(Base64.getDecoder().decode(cipher)), StandardCharsets.UTF_8);
    }

    private byte[] xor(byte[] data) {
        byte[] k = key.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = (byte) (data[i] ^ k[i % k.length]);
        }
        return out;
    }
}
