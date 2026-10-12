
package com.laptopstore.service;

import com.laptopstore.dao.JdbcLaptopDAO;
import com.laptopstore.dao.JdbcStockMovementDAO;
import com.laptopstore.model.Laptop;

import java.sql.SQLException;

public class StockServiceDemo {

    public static void main(String[] args) {

        StockService stockService = new StockService();

        JdbcLaptopDAO laptopDAO = new JdbcLaptopDAO();

        JdbcStockMovementDAO movementDAO =
                new JdbcStockMovementDAO();

        // Laptop mau trong database
        int laptopId = 1;

        try {
            Laptop before = laptopDAO.findById(laptopId);

            if (before == null) {
                System.out.println("Laptop khong ton tai!");
                return;
            }

            int oldQuantity = before.getQuantity();

            int oldHistoryCount =
                    movementDAO.findByLaptopId(laptopId).size();

            System.out.println("=== TRUOC KHI KIEM THU ===");
            System.out.println("Laptop: " + before.getLaptopName());
            System.out.println("Ton kho: " + oldQuantity);

            // 1. Nhap them 2 chiec
            boolean imported =
                    stockService.importStock(laptopId, null, 2);

            System.out.println("\n=== NHAP KHO ===");
            System.out.println("Ket qua: " + imported);

            // 2. Xuat 1 chiec
            boolean exported =
                    stockService.exportStock(laptopId, null, 1);

            System.out.println("\n=== XUAT KHO ===");
            System.out.println("Ket qua: " + exported);

            // 3. Thu xuat vuot so luong ton
            boolean overExported =
                    stockService.exportStock(
                            laptopId, null, Integer.MAX_VALUE
                    );

            System.out.println("\n=== XUAT VUOT TON KHO ===");
            System.out.println("Ket qua: " + overExported);

            // 4. Kiem tra lai database
            Laptop after = laptopDAO.findById(laptopId);

            int newHistoryCount =
                    movementDAO.findByLaptopId(laptopId).size();

            System.out.println("\n=== KET QUA CUOI ===");

            System.out.println(
                    "Ton kho truoc: " + oldQuantity
            );

            System.out.println(
                    "Ton kho sau: " + after.getQuantity()
            );

            System.out.println(
                    "So lich su moi: "
                            + (newHistoryCount - oldHistoryCount)
            );

            boolean passed =
                    imported
                            && exported
                            && !overExported
                            && after.getQuantity() == oldQuantity + 1
                            && newHistoryCount == oldHistoryCount + 2;

            System.out.println(
                    "KIEM THU: "
                            + (passed ? "PASS" : "FAIL")
            );

        } catch (SQLException e) {
            System.out.println(
                    "Loi MySQL: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}
