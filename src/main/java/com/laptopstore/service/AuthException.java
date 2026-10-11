package com.laptopstore.service;

/** Lỗi nghiệp vụ xác thực/đăng ký; message hiển thị trực tiếp cho người dùng. */
public class AuthException extends Exception {
    public AuthException(String message) { super(message); }
    public AuthException(String message, Throwable cause) { super(message, cause); }
}
