package com.laptopstore.model;

/**
 * Loai giao dich kho.
 * Gia tri name() trung voi chuoi dang luu trong cot movement_type
 * ("IMPORT", "EXPORT") nen dung duoc song song voi StockMovement hien co.
 */
public enum StockMovementType {

    IMPORT("Nhap kho"),
    EXPORT("Xuat kho");

    private final String displayName;

    StockMovementType(String displayName) {
        this.displayName = displayName;
    }

    // Ten hien thi tren giao dien
    public String getDisplayName() {
        return displayName;
    }

    // Chuyen chuoi lay tu database ("IMPORT"/"EXPORT") thanh enum
    public static StockMovementType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null; // Đổi throw Exception thành return null
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Đổi throw Exception thành return null
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
