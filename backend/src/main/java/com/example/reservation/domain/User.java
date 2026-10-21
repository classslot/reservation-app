package com.example.reservation.domain;

import jakarta.persistence.*;

/** users（利用者）: ログイン情報と権限 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected User() {}

    public User(String loginId, String passwordHash, Role role) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getLoginId() { return loginId; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
