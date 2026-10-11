package com.laptopstore.service;

import com.laptopstore.dao.AccountDAO;
import com.laptopstore.dao.JdbcAccountDAO;
import com.laptopstore.model.Account;
import com.laptopstore.model.AccountRole;

public class AuthServiceImpl implements AuthService {
    private static final int MIN_PASSWORD_LENGTH = 6;
    private final AccountDAO accountDAO;

    public AuthServiceImpl() {
        this(new JdbcAccountDAO());
    }

    /** Cho phép truyền DAO khác (vd: InMemoryAccountDAO khi test). */
    public AuthServiceImpl(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @Override
    public Account login(String username, char[] password) throws Exception {
        try {
            if (username == null || username.trim().isEmpty() || password == null || password.length == 0) {
                throw new AuthException("Vui lòng nhập tên đăng nhập và mật khẩu");
            }
            Account account = accountDAO.findByUsername(username.trim());
            // Cùng một thông báo cho "không tồn tại" và "sai mật khẩu" để không lộ username hợp lệ.
            if (account == null || account.getPasswordSalt() == null || account.getPasswordHash() == null
                    || !PasswordHasher.verify(password, account.getPasswordSalt(), account.getPasswordHash())) {
                throw new AuthException("Sai tên đăng nhập hoặc mật khẩu");
            }
            if (!account.isActive()) {
                throw new AuthException("Tài khoản đã bị khóa, vui lòng liên hệ quản trị viên");
            }
            return account;
        } finally {
            PasswordHasher.wipe(password);
        }
    }

    @Override
    public Account register(String username, char[] password, String fullName) throws Exception {
        try {
            if (username == null || username.trim().length() < 3) {
                throw new AuthException("Tên đăng nhập phải có ít nhất 3 ký tự");
            }
            if (!username.trim().matches("[A-Za-z0-9_.]+")) {
                throw new AuthException("Tên đăng nhập chỉ gồm chữ, số, dấu chấm và gạch dưới");
            }
            if (fullName == null || fullName.trim().isEmpty()) {
                throw new AuthException("Vui lòng nhập họ tên");
            }
            if (password == null || password.length < MIN_PASSWORD_LENGTH) {
                throw new AuthException("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
            }
            if (accountDAO.existsByUsername(username.trim())) {
                throw new AuthException("Tên đăng nhập đã tồn tại");
            }
            String salt = PasswordHasher.newSalt();
            String hash = PasswordHasher.hash(password, salt);
            // Đăng ký công khai chỉ được tạo STAFF; ADMIN do quản trị cấp trong DB.
            Account account = new Account(0, username.trim(), hash, salt, fullName.trim(), AccountRole.STAFF, true);
            accountDAO.insert(account);
            return account;
        } finally {
            PasswordHasher.wipe(password);
        }
    }
}
