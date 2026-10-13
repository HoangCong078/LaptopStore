package com.laptopstore.dao;

import com.laptopstore.config.DatabaseConnection;
import com.laptopstore.model.Warranty;
import com.laptopstore.model.WarrantyStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object quản lý dữ liệu bảo hành sản phẩm.
 * Sử dụng 100% PreparedStatement, đóng tài nguyên bằng try-with-resources,
 * không sử dụng SELECT * và không can thiệp transaction trong createWarranty.
 */
public class WarrantyDAO {

    /**
     * Tính ngày hết hạn bảo hành (mặc định là 12 tháng kể từ ngày bắt đầu).
     * Tách thành static method để phục vụ unit test độc lập không cần DB.
     *
     * @param start ngày bắt đầu bảo hành
     * @return ngày hết hạn bảo hành sau 12 tháng
     */
    public static LocalDate calculateEndDate(LocalDate start) {
        if (start == null) {
            start = LocalDate.now();
        }
        return start.plusMonths(12);
    }

    /**
     * Tạo thẻ bảo hành mới trong cùng transaction bán hàng của SaleService.
     * <p>
     * QUY TẮC QUAN TRỌNG:
     * - Dùng đúng Connection được truyền vào cho mọi câu lệnh.
     * - TUYỆT ĐỐI KHÔNG gọi commit, rollback, setAutoCommit hoặc close trên Connection này.
     * - Chỉ ném SQLException để SaleService điều phối rollback.
     *
     * @param conn Connection từ transaction của SaleService
     * @param w    thông tin bảo hành ban đầu
     * @return số thẻ bảo hành đã tạo thành công
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu hoặc không tìm thấy invoice_detail
     */
    public int createWarranty(Connection conn, Warranty w) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection được truyền vào createWarranty không được null");
        }
        if (w == null) {
            throw new SQLException("Dữ liệu Warranty không được null");
        }

        // ASSUMPTION: Bảng chi tiết hóa đơn trong schema.sql có tên là invoice_details với khóa chính detail_id
        String checkQuantitySql = "SELECT quantity FROM invoice_details WHERE detail_id = ?";
        int quantity;
        try (PreparedStatement psCheck = conn.prepareStatement(checkQuantitySql)) {
            psCheck.setInt(1, w.getInvoiceDetailId());
            try (ResultSet rs = psCheck.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy chi tiết hóa đơn (invoice_details) với ID: " + w.getInvoiceDetailId());
                }
                quantity = rs.getInt("quantity");
            }
        }

        if (quantity <= 0) {
            throw new SQLException("Số lượng sản phẩm trong chi tiết hóa đơn không hợp lệ: " + quantity);
        }

        LocalDate startDate = (w.getStartDate() != null) ? w.getStartDate() : LocalDate.now();
        LocalDate endDate = calculateEndDate(startDate);

        // ASSUMPTION: Bảng bảo hành được đặt tên là warranty theo thiết kế đề xuất
        String insertSql = "INSERT INTO warranty (invoice_detail_id, serial_number, start_date, end_date, status, note) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
            for (int i = 0; i < quantity; i++) {
                psInsert.setInt(1, w.getInvoiceDetailId());

                // Nếu số lượng là 1 và có truyền serial_number thì lưu;
                // Nếu mua nhiều máy (quantity > 1), các thẻ để NULL serial để nhân viên gán sau, tránh trùng khóa UNIQUE
                if (i == 0 && quantity == 1 && w.getSerialNumber() != null && !w.getSerialNumber().trim().isEmpty()) {
                    psInsert.setString(2, w.getSerialNumber().trim());
                } else {
                    psInsert.setNull(2, Types.VARCHAR);
                }

                psInsert.setDate(3, Date.valueOf(startDate));
                psInsert.setDate(4, Date.valueOf(endDate));
                psInsert.setString(5, WarrantyStatus.ACTIVE.name());

                if (w.getNote() != null && !w.getNote().trim().isEmpty()) {
                    psInsert.setString(6, w.getNote().trim());
                } else {
                    psInsert.setNull(6, Types.VARCHAR);
                }

                psInsert.addBatch();
            }

            int[] batchResults = psInsert.executeBatch();
            return batchResults.length;
        }
    }

    /**
     * Tra cứu danh sách bảo hành theo số điện thoại khách hàng.
     *
     * @param phone số điện thoại cần tra cứu
     * @return danh sách thẻ bảo hành sắp xếp theo end_date tăng dần
     * @throws SQLException nếu xảy ra lỗi truy vấn DB
     */
    public List<Warranty> searchByPhone(String phone) throws SQLException {
        // ASSUMPTION: Hỗ trợ cả 2 định dạng: 0912345678 và +84912345678 phòng trường hợp SaleService lưu định dạng quốc tế
        String altPhone = (phone != null && phone.startsWith("0")) ? "+84" + phone.substring(1) : phone;
        String sql = buildSearchBaseQuery() + " WHERE (inv.customer_phone = ? OR inv.customer_phone = ?) ORDER BY w.end_date ASC LIMIT 200";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setString(2, altPhone);
            try (ResultSet rs = ps.executeQuery()) {
                return extractWarranties(rs);
            }
        }
    }

    /**
     * Tra cứu danh sách bảo hành theo mã hóa đơn.
     *
     * @param invoiceId mã hóa đơn cần tra cứu
     * @return danh sách thẻ bảo hành sắp xếp theo end_date tăng dần
     * @throws SQLException nếu xảy ra lỗi truy vấn DB
     */
    public List<Warranty> searchByInvoiceId(int invoiceId) throws SQLException {
        // ASSUMPTION: inv.invoice_id là khóa chính bảng invoices
        String sql = buildSearchBaseQuery() + " WHERE inv.invoice_id = ? ORDER BY w.end_date ASC LIMIT 200";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                return extractWarranties(rs);
            }
        }
    }

    /**
     * Tra cứu thông tin bảo hành theo số serial của máy.
     *
     * @param serialNumber số serial cần tra cứu
     * @return danh sách thẻ bảo hành khớp số serial (thường có 1 kết quả)
     * @throws SQLException nếu xảy ra lỗi truy vấn DB
     */
    public List<Warranty> searchBySerialNumber(String serialNumber) throws SQLException {
        String sql = buildSearchBaseQuery() + " WHERE w.serial_number = ? ORDER BY w.end_date ASC LIMIT 50";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serialNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return extractWarranties(rs);
            }
        }
    }

    /**
     * Cập nhật trạng thái của thẻ bảo hành.
     *
     * @param warrantyId mã thẻ bảo hành (id)
     * @param newStatus  trạng thái mới
     * @return true nếu cập nhật thành công, false nếu không tìm thấy bản ghi
     * @throws SQLException nếu xảy ra lỗi khi thực thi câu lệnh SQL
     */
    public boolean updateStatus(int warrantyId, WarrantyStatus newStatus) throws SQLException {
        String sql = "UPDATE warranty SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, warrantyId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Tự động quét và chuyển sang trạng thái EXPIRED cho các thẻ ACTIVE có ngày hết hạn < CURDATE().
     * Các thẻ đang PROCESSING (đang tiếp nhận xử lý) sẽ giữ nguyên để tiếp tục bảo hành cho khách.
     *
     * @return số bản ghi được cập nhật trạng thái sang EXPIRED
     * @throws SQLException nếu xảy ra lỗi truy vấn DB
     */
    public int expireOverdue() throws SQLException {
        String sql = "UPDATE warranty SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND end_date < CURDATE()";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    /**
     * Khung truy vấn JOIN chung giữa warranty, invoice_details, invoices và laptops.
     * Không sử dụng SELECT *, chỉ lấy đúng các cột cần thiết cho việc xử lý và hiển thị.
     */
    private String buildSearchBaseQuery() {
        // ASSUMPTION: Bảng warranty JOIN invoice_details qua invoice_detail_id = detail_id,
        // JOIN invoices qua invoice_id, và LEFT JOIN laptops qua laptop_id
        return "SELECT w.id, w.invoice_detail_id, w.serial_number, w.start_date, w.end_date, w.status, w.note, "
                + "inv.invoice_id, inv.customer_name, inv.customer_phone, lap.laptop_name AS product_name "
                + "FROM warranty w "
                + "JOIN invoice_details idt ON w.invoice_detail_id = idt.detail_id "
                + "JOIN invoices inv ON idt.invoice_id = inv.invoice_id "
                + "LEFT JOIN laptops lap ON idt.laptop_id = lap.laptop_id";
    }

    /**
     * Ánh xạ tập kết quả ResultSet thành danh sách các đối tượng Warranty.
     */
    private List<Warranty> extractWarranties(ResultSet rs) throws SQLException {
        List<Warranty> list = new ArrayList<>();
        while (rs.next()) {
            Warranty w = new Warranty();
            w.setId(rs.getInt("id"));
            w.setInvoiceDetailId(rs.getInt("invoice_detail_id"));
            w.setSerialNumber(rs.getString("serial_number"));

            Date sqlStart = rs.getDate("start_date");
            if (sqlStart != null) {
                w.setStartDate(sqlStart.toLocalDate());
            }

            Date sqlEnd = rs.getDate("end_date");
            if (sqlEnd != null) {
                w.setEndDate(sqlEnd.toLocalDate());
            }

            String statusStr = rs.getString("status");
            w.setStatus(WarrantyStatus.fromString(statusStr));
            w.setNote(rs.getString("note"));

            // Các trường JOIN để hiển thị
            w.setInvoiceId(rs.getInt("invoice_id"));
            w.setCustomerName(rs.getString("customer_name"));
            w.setCustomerPhone(rs.getString("customer_phone"));
            w.setProductName(rs.getString("product_name"));

            list.add(w);
        }
        return list;
    }

    /**
     * Lấy kết nối cơ sở dữ liệu từ DatabaseConnection chung.
     */
    private Connection getConnection() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Không thể kết nối đến cơ sở dữ liệu. Vui lòng kiểm tra config.properties!");
        }
        return conn;
    }
}
