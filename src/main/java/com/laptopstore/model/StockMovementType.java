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
            throw new IllegalArgumentException(
                    "Loai giao dich khong duoc de trong"
            );
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Loai giao dich phai la IMPORT hoac EXPORT"
            );
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
