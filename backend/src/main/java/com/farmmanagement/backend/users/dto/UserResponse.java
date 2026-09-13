package com.farmmanagement.backend.users.dto;

import com.farmmanagement.backend.auth.Role;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String phoneNumber;
    private Role role;
    private LocalDateTime createdAt;

    public UserResponse(
            Long id,
            String firstname,
            String lastname,
            String username,
            String email,
            String phoneNumber,
            Role role,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}