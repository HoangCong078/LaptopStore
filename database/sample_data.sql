USE laptop_store;

-- Thêm tài khoản mẫu (Mật khẩu đang để thô cho dễ test ban đầu)
INSERT INTO accounts (username, password, full_name, role) VALUES 
('admin', '123456', 'Quản trị viên hệ thống', 'ADMIN'),
('staff01', '123456', 'Nhân viên Bán hàng 01', 'STAFF');

-- Thêm danh mục sản phẩm mẫu
INSERT INTO categories (category_name, description) VALUES 
('Laptop Gaming', 'Laptop cấu hình cao dành cho đồ họa và game thủ'),
('Laptop Văn phòng', 'Laptop mỏng nhẹ, pin trâu, phục vụ học tập và làm việc');

-- Thêm sản phẩm (laptop) mẫu
INSERT INTO laptops (laptop_name, brand, category_id, price, quantity, cpu, ram, storage, screen) VALUES 
('Asus ROG Strix G15', 'Asus', 1, 25000000, 15, 'Core i7 12700H', '16GB', '512GB SSD', '15.6 inch 144Hz'),
('Dell XPS 13 Plus', 'Dell', 2, 32000000, 8, 'Core i5 1240P', '16GB', '512GB SSD', '13.4 inch OLED'),
('MacBook Air M2', 'Apple', 2, 27500000, 10, 'Apple M2', '8GB', '256GB SSD', '13.6 inch Retina');