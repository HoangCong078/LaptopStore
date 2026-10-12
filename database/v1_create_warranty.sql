-- =========================================================================
-- FILE SQL MIGRATION TẠO BẢNG BẢO HÀNH (WARRANTY)
-- Tách riêng theo quy định của nhóm, không can thiệp trực tiếp vào schema.sql
-- =========================================================================

USE laptop_store;

-- 1. Nếu hệ thống đã tồn tại bảng warranties sơ khai cũ từ schema ban đầu,
-- có thể xóa hoặc đổi tên để chuyển sang cấu trúc chuẩn liên kết với invoice_details.
DROP TABLE IF EXISTS warranties;

-- 2. Tạo bảng warranty theo đúng thiết kế đề xuất
-- ASSUMPTION: invoice_detail_id liên kết với khóa chính detail_id của bảng invoice_details
CREATE TABLE IF NOT EXISTS warranty (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_detail_id INT NOT NULL,
    serial_number VARCHAR(50) NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE', 'PROCESSING', 'COMPLETED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    note VARCHAR(255) NULL,
    CONSTRAINT fk_warranty_invoice_detail
        FOREIGN KEY (invoice_detail_id) REFERENCES invoice_details(detail_id)
        ON DELETE RESTRICT
);

-- 3. Chỉ mục (Indexes) tối ưu hóa hiệu năng truy vấn và quét ngày hết hạn
CREATE INDEX idx_warranty_invoice_detail ON warranty(invoice_detail_id);
CREATE INDEX idx_warranty_status_enddate ON warranty(status, end_date);
CREATE INDEX idx_warranty_enddate ON warranty(end_date ASC);

-- KHUYẾN NGHỊ NHÓM: Bổ sung chỉ mục trên bảng invoices để tối ưu hóa tra cứu theo SĐT:
-- ALTER TABLE invoices ADD INDEX idx_invoices_customer_phone (customer_phone);
