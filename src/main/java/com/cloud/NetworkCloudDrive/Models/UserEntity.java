package com.cloud.NetworkCloudDrive.Models;

import com.cloud.NetworkCloudDrive.Models.Enum.UserRole;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "mail")
    private String mail;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private UserRole role = UserRole.GUEST;

    // change to instant
    // purely for telemetry
    @Column(name = "lastLogin")
    private Instant lastLogin;

    // purely for telemetry
    @Column(name = "registeredAt")
    @CreationTimestamp
    private Instant registeredAt;

    // security
    @Column(name = "passwordUpdatedAt")
    private Instant passwordUpdatedAt;

    @Column(name = "passwordUpdateCount")
    private long passwordUpdateCount;

    @Column(name = "uses_keys")
    private boolean usesKeys;

    public UserEntity(String name, String mail, String password, UserRole role) {
        this.name = name;
        this.mail = mail;
        this.password = password;
        this.role = role;
    }

    public UserEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Instant getPasswordUpdatedAt() {
        return passwordUpdatedAt;
    }

    public void setPasswordUpdatedAt(Instant passwordUpdatedAt) {
        this.passwordUpdatedAt = passwordUpdatedAt;
    }

    public long getPasswordUpdateCount() {
        return passwordUpdateCount;
    }

    public void updatePasswordUpdatedAt() {
        this.passwordUpdatedAt = Instant.now();
    }

    public void setPasswordUpdateCount(long passwordUpdateCount) {
        this.passwordUpdateCount = passwordUpdateCount;
    }

    public void incrementPasswordUpdateCount() {
        this.passwordUpdateCount++;
    }

    public boolean isUsesKeys() {
        return usesKeys;
    }

    public void setUsesKeys(boolean usesKeys) {
        this.usesKeys = usesKeys;
    }

    public void onPasswordChange() {
        incrementPasswordUpdateCount();
        updatePasswordUpdatedAt();
    }
}
