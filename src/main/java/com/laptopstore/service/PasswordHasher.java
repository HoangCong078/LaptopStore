package com.laptopstore.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Băm mật khẩu bằng PBKDF2WithHmacSHA256 (có sẵn trong JDK, không cần thêm thư viện).
 * Định dạng lưu trong DB (cột accounts.password): "saltBase64:hashBase64".
 */
public final class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() { }

    /** Tạo salt ngẫu nhiên (Base64). */
    public static String newSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Băm mật khẩu với salt cho trước, trả về hash dạng Base64. */
    public static String hash(char[] password, String saltBase64) {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("Không thể băm mật khẩu", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** So sánh mật khẩu nhập vào với hash đã lưu (thời gian so sánh không đổi). */
    public static boolean verify(char[] password, String saltBase64, String expectedHashBase64) {
        byte[] actual = hash(password, saltBase64).getBytes(StandardCharsets.UTF_8);
        byte[] expected = expectedHashBase64.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(actual, expected);
    }

    /** Ghép salt và hash thành chuỗi lưu vào DB. */
    public static String encode(String saltBase64, String hashBase64) {
        return saltBase64 + ":" + hashBase64;
    }

    /** Tách chuỗi DB thành {salt, hash}; trả về null nếu sai định dạng (vd: mật khẩu thô cũ). */
    public static String[] decode(String stored) {
        if (stored == null) return null;
        String[] parts = stored.split(":");
        return parts.length == 2 ? parts : null;
    }

    /** Xóa mảng mật khẩu khỏi bộ nhớ sau khi dùng xong. */
    public static void wipe(char[] password) {
        if (password != null) Arrays.fill(password, '\0');
    }
}
