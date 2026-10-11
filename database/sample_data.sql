USE laptop_store;

-- Tài khoản mẫu. Mật khẩu của cả 2 tài khoản là: 123456
-- Cột password lưu theo định dạng "saltBase64:hashBase64" (PBKDF2WithHmacSHA256, 120000 vòng, 256 bit).
-- Không còn lưu mật khẩu thô. Tài khoản mới nên tạo bằng form Đăng ký.
INSERT INTO accounts (username, password, full_name, role) VALUES
('admin',   'AQIDBAUGBwgJCgsMDQ4PEA==:RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'Quản trị viên hệ thống', 'ADMIN'),
('staff01', 'ERITFBUWFxgZGhscHR4fIA==:+duRVFAWG/looCJZIh4ke+Ujhi2Mo4bLwjc0Fqg2k6I=', 'Nhân viên Bán hàng 01', 'STAFF');

-- Thêm danh mục sản phẩm mẫu
INSERT INTO categories (category_name, description) VALUES 
('Laptop Gaming', 'Laptop cấu hình cao dành cho đồ họa và game thủ'),
('Laptop Văn phòng', 'Laptop mỏng nhẹ, pin trâu, phục vụ học tập và làm việc');

-- Thêm sản phẩm (laptop) mẫu
INSERT INTO laptops (laptop_name, brand, category_id, price, quantity, cpu, ram, storage, screen) VALUES 
('Asus ROG Strix G15', 'Asus', 1, 25000000, 15, 'Core i7 12700H', '16GB', '512GB SSD', '15.6 inch 144Hz'),
('Dell XPS 13 Plus', 'Dell', 2, 32000000, 8, 'Core i5 1240P', '16GB', '512GB SSD', '13.4 inch OLED'),
('MacBook Air M2', 'Apple', 2, 27500000, 10, 'Apple M2', '8GB', '256GB SSD', '13.6 inch Retina');
