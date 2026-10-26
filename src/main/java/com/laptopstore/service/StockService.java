
package com.laptopstore.service;

import com.laptopstore.dao.LaptopDAO;
import com.laptopstore.dao.JdbcLaptopDAO;
import com.laptopstore.dao.StockMovementDAO;
import com.laptopstore.dao.JdbcStockMovementDAO;
import com.laptopstore.model.StockMovement;
import com.laptopstore.model.StockMovementType;

import java.sql.Connection;
import java.sql.SQLException;

public class StockService {

    private final LaptopDAO laptopDAO;
    private final StockMovementDAO movementDAO;

    // Constructor
    public StockService() {
        this.laptopDAO = new JdbcLaptopDAO();
        this.movementDAO = new JdbcStockMovementDAO();
    }

    // 1. Nhập kho
    public boolean importStock(
            int laptopId,
            Integer accountId,
            int quantity
    ) throws SQLException {

        return processStock(
                laptopId,
                accountId,
                quantity,
                StockMovementType.IMPORT
        );
    }

    // 2. Xuất kho
    public boolean exportStock(
            int laptopId,
            Integer accountId,
            int quantity
    ) throws SQLException {

        return processStock(
                laptopId,
                accountId,
                quantity,
                StockMovementType.EXPORT
        );
    }

    // 3. Xử lý giao dịch nhập hoặc xuất kho
    private boolean processStock(
            int laptopId,
            Integer accountId,
            int quantity,
            StockMovementType type
    ) throws SQLException {

        // Kiểm tra dữ liệu đầu vào
        StockMovement movement = new StockMovement(
                laptopId,
                accountId,
                type,
                quantity
        );

        // Mở Connection riêng cho giao dịch này
        try (Connection conn =
                     StockConnectionFactory.openConnection()) {

            // Bắt đầu Transaction
            conn.setAutoCommit(false);

            try {
                boolean updated;

                // 4. Cập nhật tồn kho
                if (type == StockMovementType.IMPORT) {

                    updated = laptopDAO.increaseStock(
                            conn,
                            laptopId,
                            quantity
                    );

                } else if (type == StockMovementType.EXPORT) {

                    updated = laptopDAO.decreaseStock(
                            conn,
                            laptopId,
                            quantity
                    );

                } else {
                    throw new IllegalArgumentException(
                            "Loại giao dịch không hợp lệ"
                    );
                }

                // Laptop không tồn tại hoặc không đủ hàng
                if (!updated) {
                    conn.rollback();
                    return false;
                }

                // 5. Lưu lịch sử trên cùng Connection
                movementDAO.insert(conn, movement);

                // 6. Cả hai thao tác đều thành công
                conn.commit();

                return true;

            } catch (SQLException | RuntimeException e) {

                // Có lỗi: hoàn tác cả hai thao tác
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }

        } // Tự động đóng Connection tại đây
    }
}
