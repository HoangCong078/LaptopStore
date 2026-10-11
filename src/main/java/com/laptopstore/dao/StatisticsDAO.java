package com.laptopstore.dao;

import com.laptopstore.config.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Thống kê cho trang chủ. Truy vấn thẳng bảng laptops/invoices nên không phụ thuộc DAO của
 * Thành viên 2, 3. Nếu họ đổi tên cột (quantity, total_amount, created_at) thì sửa tại đây.
 */
public class StatisticsDAO {

    private Connection connection() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Không thể kết nối database");
        }
        return conn;
    }

    /** Tổng số máy đang tồn kho (cộng quantity của mọi mẫu laptop). */
    public int getTotalStock() throws SQLException {
        try (PreparedStatement ps = connection().prepareStatement("SELECT COALESCE(SUM(quantity), 0) FROM laptops");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Doanh thu của các hóa đơn tạo trong ngày hôm nay. */
    public BigDecimal getTodayRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM invoices WHERE DATE(created_at) = CURDATE()";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }
}
