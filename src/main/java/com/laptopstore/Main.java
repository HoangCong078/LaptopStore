package com.laptopstore;

import com.laptopstore.config.DatabaseConnection;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        System.out.println("LaptopStore is ready!");
        
        // Kiểm tra kết nối Database
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            System.out.println(">>> Kiem tra thanh công: Da ket noi duoc voi MySQL database 'laptop_store'!");
        } else {
            System.out.println(">>> Loi: Khong the ket noi database. Hay kiem tra lai file config.properties!");
        }
    }
}