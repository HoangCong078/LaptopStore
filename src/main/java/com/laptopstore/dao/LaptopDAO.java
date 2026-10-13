
package com.laptopstore.dao;

import com.laptopstore.model.Laptop;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface LaptopDAO {

    // 1. Lay tat ca laptop
    List<Laptop> findAll() throws SQLException;

    // 2. Tim laptop theo ID
    Laptop findById(int laptopId) throws SQLException;

    // 3. Tim kiem theo ten hoac hang
    List<Laptop> search(String keyword) throws SQLException;

    // 4. Them laptop moi
    int insert(Laptop laptop) throws SQLException;

    // 5. Cap nhat thong tin laptop
    boolean update(Laptop laptop) throws SQLException;

    // 6. Xoa laptop theo ID
    boolean delete(int laptopId) throws SQLException;

    // 7. Nhap them hang vao kho
    boolean increaseStock(
            Connection conn,
            int laptopId,
            int quantity
    ) throws SQLException;

    // 8. Tru hang trong kho
    boolean decreaseStock(
            Connection conn,
            int laptopId,
            int quantity
    ) throws SQLException;
}
