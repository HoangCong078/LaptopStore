
package com.laptopstore.service;

import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Properties;

public class StockConnectionFactory {

    // Không cho tạo đối tượng trực tiếp
    private StockConnectionFactory() {
    }

    // Tạo một Connection mới cho mỗi transaction
    public static Connection openConnection()
            throws SQLException {

        Properties props = new Properties();

        // Đọc cấu hình MySQL từ resources
        try (InputStream in =
                     StockConnectionFactory.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     "config.properties"
                             )) {

            if (in == null) {
                throw new SQLException(
                        "Không tìm thấy config.properties"
                );
            }

            props.load(in);

        } catch (IOException e) {

            throw new SQLException(
                    "Không thể đọc cấu hình MySQL",
                    e
            );
        }

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        if (url == null || username == null
                || password == null) {

            throw new SQLException(
                    "Thiếu thông tin kết nối MySQL"
            );
        }

        // Mỗi lần gọi sẽ tạo một Connection riêng
        return DriverManager.getConnection(
                url,
                username,
                password
        );
    }
}
