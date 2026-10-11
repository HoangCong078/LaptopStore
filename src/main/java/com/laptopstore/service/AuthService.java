package com.laptopstore.service;

import com.laptopstore.model.Account;

/** Hợp đồng đã chốt trong INTERFACE_CONTRACT: login(String, char[]) trả về Account. */
public interface AuthService {

    /** Đăng nhập; ném AuthException nếu sai thông tin hoặc tài khoản bị khóa. */
    Account login(String username, char[] password) throws Exception;

    /** Đăng ký tài khoản mới với vai trò STAFF. */
    Account register(String username, char[] password, String fullName) throws Exception;
}
