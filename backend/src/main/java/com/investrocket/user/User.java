package com.investrocket.user;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    @Column(name = "phone_number", length = 40)
    private String phoneNumber;

    @Column(length = 100)
    private String country;

    @Column(name = "preferred_currency", nullable = false, length = 3)
    private String preferredCurrency;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() {
    }

    public User(String fullName, String email, String passwordHash) {
        this.id = UUID.randomUUID();
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = Role.USER;
        this.enabled = true;
        this.preferredCurrency = "USD";
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public String getPreferredCurrency() {
        return preferredCurrency;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void updateProfile(
            String fullName,
            String phoneNumber,
            String country,
            String preferredCurrency) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.country = country;
        this.preferredCurrency = preferredCurrency;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
    }
}
