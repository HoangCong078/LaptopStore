package com.laptopstore.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.laptopstore.model.Account;
import com.laptopstore.model.AccountRole;
import org.junit.Test;

public class AuthServiceTest {
    private final InMemoryAccountDAO dao = new InMemoryAccountDAO();
    private final AuthService auth = new AuthServiceImpl(dao);

    @Test
    public void registerThenLoginSucceeds() throws Exception {
        auth.register("nhanvien1", "matkhau123".toCharArray(), "Nguyen Van A");
        Account a = auth.login("nhanvien1", "matkhau123".toCharArray());
        assertEquals("nhanvien1", a.getUsername());
        assertEquals(AccountRole.STAFF, a.getRole());
    }

    @Test
    public void wrongPasswordIsRejected() throws Exception {
        auth.register("nhanvien1", "matkhau123".toCharArray(), "Nguyen Van A");
        try {
            auth.login("nhanvien1", "saimatkhau".toCharArray());
            fail("Phải ném AuthException");
        } catch (AuthException expected) {
            assertEquals("Sai tên đăng nhập hoặc mật khẩu", expected.getMessage());
        }
    }

    @Test
    public void unknownUserGetsSameMessage() {
        try {
            auth.login("khongtontai", "123456".toCharArray());
            fail("Phải ném AuthException");
        } catch (Exception expected) {
            assertEquals("Sai tên đăng nhập hoặc mật khẩu", expected.getMessage());
        }
    }

    @Test
    public void duplicateUsernameIsRejected() throws Exception {
        auth.register("trung", "123456".toCharArray(), "A");
        try {
            auth.register("trung", "123456".toCharArray(), "B");
            fail("Phải ném AuthException");
        } catch (AuthException expected) {
            assertEquals("Tên đăng nhập đã tồn tại", expected.getMessage());
        }
    }

    @Test
    public void shortPasswordIsRejected() {
        try {
            auth.register("user1", "123".toCharArray(), "A");
            fail("Phải ném AuthException");
        } catch (Exception expected) {
            assertTrue(expected instanceof AuthException);
        }
    }

    @Test
    public void lockedAccountCannotLogin() throws Exception {
        Account a = auth.register("khoa", "123456".toCharArray(), "A");
        a.setActive(false);
        try {
            auth.login("khoa", "123456".toCharArray());
            fail("Phải ném AuthException");
        } catch (AuthException expected) {
            assertTrue(expected.getMessage().contains("khóa"));
        }
    }

    @Test
    public void sameSaltOrHashNeverReused() {
        String s1 = PasswordHasher.newSalt();
        String s2 = PasswordHasher.newSalt();
        assertNotEquals(s1, s2);
        assertNotEquals(PasswordHasher.hash("abc".toCharArray(), s1),
                        PasswordHasher.hash("abc".toCharArray(), s2));
    }

    /** Hash trong sample_data.sql phải khớp mật khẩu 123456. */
    @Test
    public void sampleDataHashesMatch() {
        assertTrue(PasswordHasher.verify("123456".toCharArray(),
                "AQIDBAUGBwgJCgsMDQ4PEA==", "RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A="));
        assertTrue(PasswordHasher.verify("123456".toCharArray(),
                "ERITFBUWFxgZGhscHR4fIA==", "+duRVFAWG/looCJZIh4ke+Ujhi2Mo4bLwjc0Fqg2k6I="));
        assertFalse(PasswordHasher.verify("sai".toCharArray(),
                "AQIDBAUGBwgJCgsMDQ4PEA==", "RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A="));
    }
}
