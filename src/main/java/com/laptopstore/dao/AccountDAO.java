package com.laptopstore.dao;

import com.laptopstore.model.Account;
import java.sql.SQLException;

public interface AccountDAO {
    /** Trả về null nếu không có tài khoản. */
    Account findByUsername(String username) throws SQLException;

    boolean existsByUsername(String username) throws SQLException;

    /** Thêm tài khoản; cập nhật id được sinh ra vào đối tượng. */
    void insert(Account account) throws SQLException;
}
