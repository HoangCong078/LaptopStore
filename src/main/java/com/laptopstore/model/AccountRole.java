package com.laptopstore.model;

/** Vai trò tài khoản (đã chốt trong UML). */
public enum AccountRole {
    ADMIN, STAFF;

    /** Chuyển chuỗi trong DB sang enum; giá trị lạ -> STAFF (quyền thấp nhất). */
    public static AccountRole fromString(String value) {
        if (value == null) return STAFF;
        try {
            return AccountRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return STAFF;
        }
    }
}
