package com.laptopstore.model;

/**
 * Tài khoản đăng nhập. Theo UML: id, username, passwordHash, passwordSalt, role, active.
 * Bổ sung fullName vì cột accounts.full_name trong schema là NOT NULL.
 * Trong DB, salt và hash được lưu chung ở cột `password` theo dạng "salt:hash" (Base64).
 */
public class Account {
    private int id;
    private String username;
    private String passwordHash;
    private String passwordSalt;
    private String fullName;
    private AccountRole role;
    private boolean active;

    public Account(int id, String username, String passwordHash, String passwordSalt,
                   String fullName, AccountRole role, boolean active) {
        this.id = id;
        setUsername(username);
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        this.username = username.trim();
    }

    public String getPasswordHash() { return passwordHash; }
    public String getPasswordSalt() { return passwordSalt; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public AccountRole getRole() { return role; }
    public void setRole(AccountRole role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isAdmin() { return role == AccountRole.ADMIN; }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
