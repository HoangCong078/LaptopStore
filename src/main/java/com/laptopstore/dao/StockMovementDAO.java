
package com.laptopstore.dao;

import com.laptopstore.model.StockMovement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface StockMovementDAO {

    // 1. Luu mot giao dich nhap/xuat kho
    int insert(
            Connection conn,
            StockMovement movement
    ) throws SQLException;

    // 2. Lay toan bo lich su nhap/xuat kho
    List<StockMovement> findAll() throws SQLException;

    // 3. Lay lich su theo ma laptop
    List<StockMovement> findByLaptopId(
            int laptopId
    ) throws SQLException;
}
