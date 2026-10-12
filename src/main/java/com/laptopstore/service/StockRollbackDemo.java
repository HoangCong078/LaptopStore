
package com.laptopstore.service;

import com.laptopstore.dao.JdbcLaptopDAO;
import com.laptopstore.dao.JdbcStockMovementDAO;
import com.laptopstore.model.Laptop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StockRollbackDemo {

    public static void main(String[] args) {

        // Thay bang ID laptop THU NGHIEM cua ban
        int laptopId = 1;

        // Account ID dung de gay loi khoa ngoai
        int invalidAccountId = Integer.MAX_VALUE;

        StockService stockService = new StockService();
        JdbcLaptopDAO laptopDAO = new JdbcLaptopDAO();
        JdbcStockMovementDAO movementDAO =
                new JdbcStockMovementDAO();

        try {
            // Xac nhan account ID nay khong ton tai
            try (Connection conn =
                         StockConnectionFactory.openConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT 1 FROM accounts WHERE account_id = ?"
                 )) {

                ps.setInt(1, invalidAccountId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println(
                                "Account ID da ton tai. Dung kiem thu!"
                        );
                        return;
                    }
                }
            }

            Laptop before = laptopDAO.findById(laptopId);

            if (before == null) {
                System.out.println("Khong tim thay laptop!");
                return;
            }

            int oldQuantity = before.getQuantity();
            int oldHistoryCount =
                    movementDAO.findByLaptopId(laptopId).size();

            System.out.println("=== TRUOC KIEM THU ===");
            System.out.println("Ton kho: " + oldQuantity);
            System.out.println("Lich su: " + oldHistoryCount);

            boolean foreignKeyError = false;

            // Co tinh gay loi khi ghi lich su
            try {
                stockService.importStock(
                        laptopId,
                        invalidAccountId,
                        1
                );

                System.out.println(
                        "CANH BAO: Giao dich khong bao loi!"
                );

            } catch (SQLException e) {

                System.out.println(
                        "Da bat duoc loi SQL: " + e.getMessage()
                );

                foreignKeyError =
                        e.getSQLState() != null
                                && e.getSQLState().startsWith("23");
            }

            // Doc lai du lieu sau giao dich
            Laptop after = laptopDAO.findById(laptopId);

            int newHistoryCount =
                    movementDAO.findByLaptopId(laptopId).size();

            System.out.println("\n=== SAU KIEM THU ===");

            System.out.println(
                    "Ton kho: " + after.getQuantity()
            );

            System.out.println(
                    "Lich su: " + newHistoryCount
            );

            boolean passed =
                    foreignKeyError
                            && after.getQuantity() == oldQuantity
                            && newHistoryCount == oldHistoryCount;

            System.out.println(
                    "ROLLBACK TEST: "
                            + (passed ? "PASS" : "FAIL")
            );

        } catch (SQLException e) {
            System.out.println(
                    "Loi kiem thu: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}
