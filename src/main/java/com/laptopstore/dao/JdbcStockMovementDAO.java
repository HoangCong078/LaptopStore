
package com.laptopstore.dao;

import com.laptopstore.config.DatabaseConnection;
import com.laptopstore.model.StockMovement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcStockMovementDAO implements StockMovementDAO {

    // 1. Lay ket noi tu DatabaseConnection
    private Connection getConnection() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();

        if (conn == null || conn.isClosed()) {
            throw new SQLException("Khong the ket noi MySQL");
        }

        return conn;
    }

    // 2. Chuyen mot dong MySQL thanh StockMovement
    private StockMovement mapResultSet(ResultSet rs)
            throws SQLException {

        int movementId = rs.getInt("movement_id");
        int laptopId = rs.getInt("laptop_id");

        Integer accountId =
                rs.getObject("account_id", Integer.class);

        String movementType =
                rs.getString("movement_type");

        int quantity = rs.getInt("quantity");

        Timestamp timestamp = rs.getTimestamp("created_at");

        return new StockMovement(
                movementId,
                laptopId,
                accountId,
                movementType,
                quantity,
                timestamp == null
                        ? null
                        : timestamp.toLocalDateTime()
        );
    }

    // 3. Luu lich su nhap/xuat kho
    @Override
    public int insert(Connection conn,
                      StockMovement movement)
            throws SQLException {

        if (conn == null || conn.isClosed()) {
            throw new SQLException("Ket noi khong hop le");
        }

        if (movement == null) {
            throw new IllegalArgumentException(
                    "Giao dich kho khong duoc null"
            );
        }

        String sql = """
                INSERT INTO stock_movements (
                    laptop_id,
                    account_id,
                    movement_type,
                    quantity
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps =
                     conn.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            ps.setInt(1, movement.getLaptopId());

            if (movement.getAccountId() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, movement.getAccountId());
            }

            ps.setString(3, movement.getMovementType());
            ps.setInt(4, movement.getQuantity());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected != 1) {
                throw new SQLException(
                        "Khong the luu lich su kho"
                );
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    movement.setMovementId(newId);
                    return newId;
                }

                throw new SQLException(
                        "Khong lay duoc ma giao dich"
                );
            }
        }
    }

    // 4. Lay tat ca lich su nhap/xuat
    @Override
    public List<StockMovement> findAll()
            throws SQLException {

        List<StockMovement> movements = new ArrayList<>();

        String sql = """
                SELECT *
                FROM stock_movements
                ORDER BY created_at DESC, movement_id DESC
                """;

        try (PreparedStatement ps =
                     getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movements.add(mapResultSet(rs));
            }
        }

        return movements;
    }

    // 5. Lay lich su theo ma laptop

    @Override
    public List<StockMovement> findByLaptopId(int laptopId)
            throws SQLException {

        if (laptopId <= 0) {
            throw new IllegalArgumentException(
                    "Mã laptop không hợp lệ"
            );
        }

        List<StockMovement> movements = new ArrayList<>();

        String sql = """
            SELECT
                sm.*,
                a.full_name AS employee_name
            FROM stock_movements sm
            LEFT JOIN accounts a
                ON sm.account_id = a.account_id
            WHERE sm.laptop_id = ?
            ORDER BY sm.created_at DESC,
                     sm.movement_id DESC
            """;

        try (PreparedStatement ps =
                     getConnection().prepareStatement(sql)) {

            ps.setInt(1, laptopId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    // Đọc thông tin giao dịch như trước
                    StockMovement movement = mapResultSet(rs);

                    // Bổ sung tên người thực hiện
                    movement.setEmployeeName(
                            rs.getString("employee_name")
                    );

                    movements.add(movement);
                }
            }
        }

        return movements;
    }

}
