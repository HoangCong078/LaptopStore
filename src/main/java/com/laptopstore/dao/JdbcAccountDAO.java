package com.laptopstore.dao;

import com.laptopstore.config.DatabaseConnection;
import com.laptopstore.model.Account;
import com.laptopstore.model.AccountRole;
import com.laptopstore.service.PasswordHasher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Bảng accounts: account_id, username, password ("salt:hash"), full_name, role, status.
 * Lưu ý: DatabaseConnection hiện là Singleton nên KHÔNG đóng Connection ở đây,
 * chỉ đóng PreparedStatement/ResultSet. Nếu sau này đổi sang Connection mới mỗi lần gọi,
 * hãy bọc Connection trong try-with-resources.
 */
public class JdbcAccountDAO implements AccountDAO {

    private Connection connection() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Không thể kết nối database. Hãy kiểm tra file config.properties");
        }
        return conn;
    }

    @Override
    public Account findByUsername(String username) throws SQLException {
        String sql = "SELECT account_id, username, password, full_name, role, status "
                   + "FROM accounts WHERE username = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String[] saltAndHash = PasswordHasher.decode(rs.getString("password"));
                return new Account(
                        rs.getInt("account_id"),
                        rs.getString("username"),
                        saltAndHash == null ? null : saltAndHash[1],
                        saltAndHash == null ? null : saltAndHash[0],
                        rs.getString("full_name"),
                        AccountRole.fromString(rs.getString("role")),
                        rs.getInt("status") == 1);
            }
        }
    }

    @Override
    public boolean existsByUsername(String username) throws SQLException {
        try (PreparedStatement ps = connection().prepareStatement("SELECT 1 FROM accounts WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public void insert(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (username, password, full_name, role, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, PasswordHasher.encode(account.getPasswordSalt(), account.getPasswordHash()));
            ps.setString(3, account.getFullName());
            ps.setString(4, account.getRole().name());
            ps.setInt(5, account.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) account.setId(keys.getInt(1));
            }
        }
    }
}
