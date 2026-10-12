package com.laptopstore.model;

import java.time.LocalDate;

/**
 * Model đại diện cho thẻ bảo hành sản phẩm.
 * Bao gồm các trường lưu trữ trong bảng warranty và các trường mở rộng
 * chỉ phục vụ cho việc hiển thị (lấy từ invoices, invoice_details, laptops).
 */
public class Warranty {
    // Các trường lưu trữ trong bảng warranty
    private int id;
    private int invoiceDetailId;
    private String serialNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private WarrantyStatus status = WarrantyStatus.ACTIVE;
    private String note;

    // Các trường CHỈ ĐỂ HIỂN THỊ khi tra cứu (không lưu trong bảng warranty)
    private int invoiceId;
    private String customerName;
    private String customerPhone;
    private String productName;

    /**
     * Constructor rỗng mặc định.
     */
    public Warranty() {
    }

    /**
     * Constructor cho việc tạo mới thẻ bảo hành (lưu vào database).
     *
     * @param invoiceDetailId mã chi tiết hóa đơn
     * @param serialNumber    số serial của máy (có thể null)
     * @param startDate       ngày bắt đầu bảo hành
     * @param endDate         ngày hết hạn bảo hành
     * @param status          trạng thái bảo hành
     * @param note            ghi chú
     */
    public Warranty(int invoiceDetailId, String serialNumber, LocalDate startDate, LocalDate endDate,
                    WarrantyStatus status, String note) {
        this.invoiceDetailId = invoiceDetailId;
        this.serialNumber = serialNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.note = note;
    }

    /**
     * Constructor đầy đủ bao gồm cả các trường hiển thị khi truy vấn JOIN.
     */
    public Warranty(int id, int invoiceDetailId, String serialNumber, LocalDate startDate, LocalDate endDate,
                    WarrantyStatus status, String note, int invoiceId, String customerName,
                    String customerPhone, String productName) {
        this.id = id;
        this.invoiceDetailId = invoiceDetailId;
        this.serialNumber = serialNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.note = note;
        this.invoiceId = invoiceId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.productName = productName;
    }

    // --- Getter và Setter ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvoiceDetailId() {
        return invoiceDetailId;
    }

    public void setInvoiceDetailId(int invoiceDetailId) {
        this.invoiceDetailId = invoiceDetailId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public WarrantyStatus getStatus() {
        return status;
    }

    public void setStatus(WarrantyStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Override
    public String toString() {
        return "Warranty{" +
                "id=" + id +
                ", invoiceDetailId=" + invoiceDetailId +
                ", serialNumber='" + serialNumber + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", productName='" + productName + '\'' +
                '}';
    }
}
