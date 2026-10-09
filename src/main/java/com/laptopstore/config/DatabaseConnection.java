package com.laptopstore.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection = null;

    // Sử dụng Singleton pattern để chỉ tạo 1 kết nối duy nhất
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Đọc thông tin từ file config.properties
                Properties props = new Properties();
                FileInputStream in = new FileInputStream("src/main/resources/config.properties");
                props.load(in);
                in.close();

                String url = props.getProperty("db.url");
                String user = props.getProperty("db.username");
                String pass = props.getProperty("db.password");

                // Thực hiện kết nối tới MySQL
                connection = DriverManager.getConnection(url, user, pass);
                System.out.println("Ket noi Database thanh cong!");
                
            } catch (SQLException | IOException e) {
                System.out.println("Loi ket noi Database: " + e.getMessage());
            }
        }
        return connection;
    }
}