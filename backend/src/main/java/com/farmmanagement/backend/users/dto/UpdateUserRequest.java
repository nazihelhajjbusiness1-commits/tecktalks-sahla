package com.farmmanagement.backend.users.dto;

import com.farmmanagement.backend.auth.Role;
import com.farmmanagement.backend.users.UserStatus;

/**
 * Admin edit of a user account. Both fields are optional — only the ones
 * provided are changed (partial update).
 */
public class UpdateUserRequest {

    private Role role;

    private UserStatus status;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
