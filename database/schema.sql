-- Tạo cơ sở dữ liệu (Nếu chưa có)
CREATE DATABASE IF NOT EXISTS laptop_store;
USE laptop_store;

-- 1. Bảng Tài khoản (Quản lý đăng nhập, phân quyền ADMIN / STAFF)
CREATE TABLE IF NOT EXISTS accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'STAFF', 
    status TINYINT DEFAULT 1 -- 1: Hoạt động, 0: Bị khóa
);

-- 2. Bảng Danh mục sản phẩm (Laptop Gaming, Laptop Văn phòng,...)
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- 3. Bảng Sản phẩm (Laptop)
CREATE TABLE IF NOT EXISTS laptops (
    laptop_id INT AUTO_INCREMENT PRIMARY KEY,
    laptop_name VARCHAR(255) NOT NULL,
    brand VARCHAR(50) NOT NULL, 
    category_id INT,
    price DECIMAL(15, 2) NOT NULL,
    quantity INT DEFAULT 0,
    cpu VARCHAR(100),
    ram VARCHAR(50),
    storage VARCHAR(100),
    screen VARCHAR(100),
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL
);

-- 4. Bảng Hóa đơn bán hàng (Sales / Invoices)
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT, -- Nhân viên lập hóa đơn
    customer_name VARCHAR(100),
    customer_phone VARCHAR(20),
    total_amount DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);

-- 5. Bảng Chi tiết hóa đơn (Invoice Details)
CREATE TABLE IF NOT EXISTS invoice_details (
    detail_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT,
    laptop_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE CASCADE,
    FOREIGN KEY (laptop_id) REFERENCES laptops(laptop_id) ON DELETE SET NULL
);

-- 6. Bảng Quản lý Bảo hành (Warranty Tracking)
CREATE TABLE IF NOT EXISTS warranties (
    warranty_id INT AUTO_INCREMENT PRIMARY KEY,
    laptop_id INT,
    customer_name VARCHAR(100),
    customer_phone VARCHAR(20),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL, -- Thời hạn bảo hành (VD: 12 tháng)
    status VARCHAR(30) DEFAULT 'ACTIVE', -- ACTIVE, RECEIVED, PROCESSING, COMPLETED
    FOREIGN KEY (laptop_id) REFERENCES laptops(laptop_id) ON DELETE SET NULL
);

-- 7. Bảng Lịch sử kho (Stock Movement)
CREATE TABLE IF NOT EXISTS stock_movements (
    movement_id INT AUTO_INCREMENT PRIMARY KEY,
    laptop_id INT,
    account_id INT,
    movement_type VARCHAR(20), -- IMPORT (Nhập kho), EXPORT (Xuất/Bán)
    quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (laptop_id) REFERENCES laptops(laptop_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);