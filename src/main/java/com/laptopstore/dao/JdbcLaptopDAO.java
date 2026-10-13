
package com.laptopstore.dao;

import com.laptopstore.config.DatabaseConnection;
import com.laptopstore.model.Laptop;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class JdbcLaptopDAO implements LaptopDAO {

    // 1. Lay ket noi MySQL
    private Connection getConnection() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();

        if (conn == null || conn.isClosed()) {
            throw new SQLException("Khong the ket noi MySQL");
        }

        return conn;
    }

    // 2. Lay danh sach laptop tu MySQL
    @Override
    public List<Laptop> findAll() throws SQLException {

        List<Laptop> laptops = new ArrayList<>();

        String sql = "SELECT * FROM laptops ORDER BY laptop_id";

        try (PreparedStatement ps =
                     getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int laptopId = rs.getInt("laptop_id");

                String laptopName =
                        rs.getString("laptop_name");

                String brand = rs.getString("brand");

                Integer categoryId =
                        rs.getObject("category_id", Integer.class);

                BigDecimal price = rs.getBigDecimal("price");

                int quantity = rs.getInt("quantity");

                String cpu = rs.getString("cpu");
                String ram = rs.getString("ram");

                String storage = rs.getString("storage");

                String screen = rs.getString("screen");
                String tags = rs.getString("tags");

                Laptop laptop = new Laptop(
                        laptopId,
                        laptopName,
                        brand,
                        categoryId,
                        price,
                        quantity,
                        cpu,
                        ram,
                        storage,
                        screen,
                        tags
                );

                laptops.add(laptop);
            }
        }

        return laptops;
    }

    // 3. Cac chuc nang se lam o buoc tiep theo


    @Override
    public Laptop findById(int laptopId) throws SQLException {

        String sql = "SELECT * FROM laptops WHERE laptop_id = ?";

        try (PreparedStatement ps =
                     getConnection().prepareStatement(sql)) {

            // Gan ID can tim vao dau ?
            ps.setInt(1, laptopId);

            try (ResultSet rs = ps.executeQuery()) {

                // Khong tim thay laptop
                if (!rs.next()) {
                    return null;
                }

                // Doc thong tin tu MySQL
                int id = rs.getInt("laptop_id");
                String name = rs.getString("laptop_name");
                String brand = rs.getString("brand");

                Integer categoryId =
                        rs.getObject("category_id", Integer.class);

                BigDecimal price = rs.getBigDecimal("price");
                int quantity = rs.getInt("quantity");

                String cpu = rs.getString("cpu");
                String ram = rs.getString("ram");
                String storage = rs.getString("storage");
                String screen = rs.getString("screen");
                String tags = rs.getString("tags");

                return new Laptop(
                        id,
                        name,
                        brand,
                        categoryId,
                        price,
                        quantity,
                        cpu,
                        ram,
                        storage,
                        screen,
                        tags
                );
            }
        }
    }



    @Override
    public List<Laptop> search(String keyword) throws SQLException {

        List<Laptop> laptops = new ArrayList<>();

        String sql = """
            SELECT *
            FROM laptops
            WHERE laptop_name LIKE ?
               OR brand LIKE ?
            ORDER BY laptop_id
            """;

        // Neu khong nhap tu khoa thi tim tat ca
        if (keyword == null) {
            keyword = "";
        }

        String searchKeyword = "%" + keyword.trim() + "%";

        try (PreparedStatement ps =
                     getConnection().prepareStatement(sql)) {

            ps.setString(1, searchKeyword);
            ps.setString(2, searchKeyword);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    int id = rs.getInt("laptop_id");

                    String name = rs.getString("laptop_name");
                    String brand = rs.getString("brand");

                    Integer categoryId =
                            rs.getObject("category_id", Integer.class);

                    BigDecimal price = rs.getBigDecimal("price");
                    int quantity = rs.getInt("quantity");

                    String cpu = rs.getString("cpu");
                    String ram = rs.getString("ram");
                    String storage = rs.getString("storage");
                    String screen = rs.getString("screen");
                    String tags = rs.getString("tags");

                    Laptop laptop = new Laptop(
                            id,
                            name,
                            brand,
                            categoryId,
                            price,
                            quantity,
                            cpu,
                            ram,
                            storage,
                            screen,
                            tags
                    );

                    laptops.add(laptop);
                }
            }
        }

        return laptops;
    }



    @Override
    public int insert(Laptop laptop) throws SQLException {

        if (laptop == null) {
            throw new IllegalArgumentException(
                    "Laptop khong duoc null"
            );
        }

        // San pham moi phai bat dau voi ton kho = 0.
        // So luong se duoc cap nhat qua nghiep vu nhap kho.
        if (laptop.getQuantity() != 0) {
            throw new IllegalArgumentException(
                    "Laptop moi phai co so luong ban dau bang 0"
            );
        }

        String sql = """
            INSERT INTO laptops (
                laptop_name,
                brand,
                category_id,
                price,
                quantity,
                cpu,
                ram,
                storage,
                screen,
                tags
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps =
                     getConnection().prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            ps.setString(1, laptop.getLaptopName());
            ps.setString(2, laptop.getBrand());

            // Category ID co the NULL
            if (laptop.getCategoryId() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, laptop.getCategoryId());
            }

            ps.setBigDecimal(4, laptop.getPrice());
            ps.setInt(5, laptop.getQuantity());

            ps.setString(6, laptop.getCpu());
            ps.setString(7, laptop.getRam());
            ps.setString(8, laptop.getStorage());
            ps.setString(9, laptop.getScreen());
            ps.setString(10, laptop.getTags());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException(
                        "Them laptop that bai"
                );
            }

            // Lay ID MySQL vua tao
            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    int newId = rs.getInt(1);
                    laptop.setLaptopId(newId);
                    return newId;
                }

                throw new SQLException(
                        "Khong lay duoc ID laptop vua tao"
                );
            }
        }
    }



    @Override
    public boolean update(Laptop laptop) throws SQLException {

        if (laptop == null) {
            throw new IllegalArgumentException(
                    "Laptop khong duoc null"
            );
        }

        if (laptop.getLaptopId() <= 0) {
            throw new IllegalArgumentException(
                    "Ma laptop khong hop le"
            );
        }

        String sql = """
            UPDATE laptops
            SET laptop_name = ?,
                brand = ?,
                category_id = ?,
                price = ?,
                cpu = ?,
                ram = ?,
                storage = ?,
                screen = ?,
                tags = ?
            WHERE laptop_id = ?
            """;

        try (PreparedStatement ps =
                     getConnection().prepareStatement(sql)) {

            ps.setString(1, laptop.getLaptopName());
            ps.setString(2, laptop.getBrand());

            if (laptop.getCategoryId() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, laptop.getCategoryId());
            }

            ps.setBigDecimal(4, laptop.getPrice());
            ps.setString(5, laptop.getCpu());
            ps.setString(6, laptop.getRam());
            ps.setString(7, laptop.getStorage());
            ps.setString(8, laptop.getScreen());
            ps.setString(9, laptop.getTags());

            ps.setInt(10, laptop.getLaptopId());

            int rowsAffected = ps.executeUpdate();

            return rowsAffected > 0;
        }
    }



    @Override
    public boolean delete(int laptopId) throws SQLException {

        if (laptopId <= 0) {
            throw new IllegalArgumentException(
                    "Ma laptop khong hop le"
            );
        }

        String sql = """
            DELETE FROM laptops
            WHERE laptop_id = ?
              AND quantity = 0
              AND NOT EXISTS (
                  SELECT 1 FROM stock_movements
                  WHERE stock_movements.laptop_id = laptops.laptop_id
              )
              AND NOT EXISTS (
                  SELECT 1 FROM invoice_details
                  WHERE invoice_details.laptop_id = laptops.laptop_id
              )
              AND NOT EXISTS (
                  SELECT 1 FROM warranties
                  WHERE warranties.laptop_id = laptops.laptop_id
              )
            """;

        try (PreparedStatement ps =
                     getConnection().prepareStatement(sql)) {

            ps.setInt(1, laptopId);

            int rowsAffected = ps.executeUpdate();

            return rowsAffected > 0;
        }
    }



    @Override
    public boolean increaseStock(
            Connection conn,
            int laptopId,
            int quantity
    ) throws SQLException {

        // 1. Kiem tra du lieu
        if (laptopId <= 0) {
            throw new IllegalArgumentException(
                    "Ma laptop khong hop le"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "So luong nhap phai lon hon 0"
            );
        }

        if (conn == null || conn.isClosed()) {
            throw new SQLException(
                    "Ket noi database khong hop le"
            );
        }

        // 2. Cau lenh cap nhat ton kho
        String sql = """
            UPDATE laptops
            SET quantity = quantity + ?
            WHERE laptop_id = ?
              AND quantity <= ?
            """;

        // 3. Thuc thi SQL
        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, laptopId);

            // Ngan tran so nguyen int
            ps.setInt(3, Integer.MAX_VALUE - quantity);

            int rowsAffected = ps.executeUpdate();

            // true neu cap nhat dung 1 laptop
            return rowsAffected == 1;
        }
    }



    @Override
    public boolean decreaseStock(
            Connection conn,
            int laptopId,
            int quantity
    ) throws SQLException {

        // 1. Kiem tra ma laptop
        if (laptopId <= 0) {
            throw new IllegalArgumentException(
                    "Ma laptop khong hop le"
            );
        }

        // 2. Kiem tra so luong xuat
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "So luong xuat phai lon hon 0"
            );
        }

        // 3. Kiem tra ket noi
        if (conn == null || conn.isClosed()) {
            throw new SQLException(
                    "Ket noi database khong hop le"
            );
        }

        // 4. SQL xuat kho
        String sql = """
            UPDATE laptops
            SET quantity = quantity - ?
            WHERE laptop_id = ?
              AND quantity >= ?
            """;

        // 5. Thuc thi SQL
        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, laptopId);
            ps.setInt(3, quantity);

            int rowsAffected = ps.executeUpdate();

            return rowsAffected == 1;
        }
    }

}
