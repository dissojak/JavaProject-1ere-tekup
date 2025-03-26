package com.stoonproduction.jobapplicatio.models;
import java.time.LocalDateTime;

public class User {
    private Long id;
    private String email;
    private String password;
    private UserRole role;
    private LocalDateTime createdAt;

    // Constructor without ID (for new entities)
    public User(String email, String password, UserRole role) {
        this(null, email, password, role, LocalDateTime.now());
    }

    // Full constructor (for loading existing entities)
    public User(Long id, String email, String password, UserRole role, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }

    public enum UserRole {
        EMPLOYER, JOB_SEEKER
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}