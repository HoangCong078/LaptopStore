
package com.laptopstore.dao;

import com.laptopstore.model.Laptop;
import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;
public class LaptopDaoDemo {

    public static void main(String[] args) {

        LaptopDAO laptopDAO = new JdbcLaptopDAO();

        try {
            // Lay danh sach laptop tu MySQL
            List<Laptop> laptops = laptopDAO.findAll();

            System.out.println("=== DANH SACH LAPTOP ===");

            if (laptops.isEmpty()) {
                System.out.println(
                        "Database chua co du lieu laptop!"
                );
                return;
            }

            for (Laptop laptop : laptops) {
                System.out.println(laptop);
            }

            System.out.println(
                    "Tong so mau laptop: " + laptops.size()
            );

            System.out.println("\n=== TIM LAPTOP THEO ID ===");

            int idCanTim = 999;

            Laptop laptop = laptopDAO.findById(idCanTim);

            if (laptop != null) {
                System.out.println("Tim thay laptop:");
                System.out.println(laptop);
            } else {
                System.out.println(
                        "Khong tim thay laptop co ID = " + idCanTim
                );
            }

            System.out.println("\n=== TIM KIEM LAPTOP ===");

            String keyword = "Dell";

            List<Laptop> ketQua = laptopDAO.search(keyword);

            System.out.println("Tu khoa: " + keyword);

            if (ketQua.isEmpty()) {
                System.out.println("Khong tim thay laptop!");
            } else {
                for (Laptop searchLaptop : ketQua) {
                    System.out.println(searchLaptop);
                }
            }

            System.out.println(
                    "So ket qua tim thay: " + ketQua.size()
            );

// ===== KIEM THU THEM LAPTOP =====

            System.out.println("\n=== THEM LAPTOP MOI ===");

            Laptop newLaptop = new Laptop(
                    "Lenovo LOQ 15",
                    "Lenovo",
                    null,
                    new BigDecimal("21500000"),
                    0,
                    "Intel Core i5",
                    "16GB",
                    "512GB SSD",
                    "15.6 inch",
                    "Gaming, Sinh vien"
            );

            int newId = laptopDAO.insert(newLaptop);

            System.out.println("Them laptop thanh cong!");
            System.out.println("ID moi: " + newId);

// Doc lai tu MySQL de kiem tra
            Laptop savedLaptop = laptopDAO.findById(newId);

            System.out.println("Du lieu da luu:");
            System.out.println(savedLaptop);

// ===== KIEM THU UPDATE LAPTOP =====

            System.out.println("\n=== CAP NHAT LAPTOP ===");

// Thay 4 bang ID Lenovo LOQ da them o buoc truoc
            int idCanSua = 4;

            Laptop laptopCanSua = laptopDAO.findById(idCanSua);

            if (laptopCanSua == null) {

                System.out.println("Khong tim thay laptop!");

            } else {

                System.out.println("Truoc khi sua:");
                System.out.println(laptopCanSua);

                // Thay doi thong tin
                laptopCanSua.setLaptopName("Lenovo LOQ 15IRX9");
                laptopCanSua.setPrice(
                        new BigDecimal("22000000")
                );
                laptopCanSua.setTags("Gaming, Lap trinh");

                // Goi DAO de cap nhat MySQL
                boolean updated = laptopDAO.update(laptopCanSua);

                System.out.println("Ket qua update: " + updated);

                // Doc lai tu MySQL
                Laptop afterUpdate = laptopDAO.findById(idCanSua);

                System.out.println("Sau khi sua:");
                System.out.println(afterUpdate);
            }

// ===== KIEM THU XOA LAPTOP =====

            System.out.println("\n=== XOA LAPTOP ===");

// Thay bang ID cua Lenovo LOQ ban da tao
            int idCanXoa = 4;

            Laptop laptopCanXoa = laptopDAO.findById(idCanXoa);

            if (laptopCanXoa == null) {

                System.out.println("Laptop khong ton tai!");

            } else if (!laptopCanXoa.getLaptopName()
                    .startsWith("Lenovo LOQ")) {

                System.out.println(
                        "Khong xoa vi day khong phai laptop thu nghiem!"
                );

            } else {

                System.out.println("Laptop truoc khi xoa:");
                System.out.println(laptopCanXoa);

                boolean deleted = laptopDAO.delete(idCanXoa);

                System.out.println("Ket qua xoa: " + deleted);

                Laptop afterDelete = laptopDAO.findById(idCanXoa);

                if (deleted && afterDelete == null) {
                    System.out.println("Da xoa laptop thanh cong!");
                } else {
                    System.out.println(
                            "Laptop khong du dieu kien xoa."
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Loi truy van MySQL: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}
