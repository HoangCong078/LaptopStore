package com.laptopstore.model;

/**
 * Định nghĩa các trạng thái của thẻ bảo hành.
 */
public enum WarrantyStatus {
    ACTIVE("Còn hiệu lực"),
    PROCESSING("Đang xử lý"),
    COMPLETED("Đã hoàn tất"),
    EXPIRED("Hết hạn");

    private final String label;

    WarrantyStatus(String label) {
        this.label = label;
    }

    /**
     * Lấy nhãn tiếng Việt của trạng thái để hiển thị trên giao diện.
     *
     * @return nhãn trạng thái tiếng Việt
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Chuyển đổi an toàn từ tên enum hoặc nhãn sang WarrantyStatus.
     *
     * @param text chuỗi tên hoặc nhãn trạng thái
     * @return WarrantyStatus tương ứng, mặc định là ACTIVE nếu không tìm thấy
     */
    public static WarrantyStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return ACTIVE;
        }
        for (WarrantyStatus status : values()) {
            if (status.name().equalsIgnoreCase(text.trim()) || status.label.equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        return ACTIVE;
    }
}
