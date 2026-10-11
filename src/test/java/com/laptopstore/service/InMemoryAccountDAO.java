package com.laptopstore.service;

import com.laptopstore.dao.AccountDAO;
import com.laptopstore.model.Account;
import java.util.HashMap;
import java.util.Map;

/** DAO giả lưu trong bộ nhớ để test AuthService mà không cần MySQL (minh họa tính đa hình). */
class InMemoryAccountDAO implements AccountDAO {
    private final Map<String, Account> accounts = new HashMap<>();
    private int nextId = 1;

    @Override
    public Account findByUsername(String username) { return accounts.get(username); }

    @Override
    public boolean existsByUsername(String username) { return accounts.containsKey(username); }

    @Override
    public void insert(Account account) {
        account.setId(nextId++);
        accounts.put(account.getUsername(), account);
    }
}
